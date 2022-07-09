package pl.jill.webmsn.GatewayService

import net.sf.jml.{Email, MsnProtocol, MsnUserStatus}
import pl.jill.webmsn.GatewayService.Exceptions._
import pl.jill.webmsn.MSN.{BoundMessengerClient, EscargotMessenger}

import scala.collection.mutable
import org.apache.logging.log4j.scala.Logging
import pl.jill.webmsn.MSN.Events.{ClientReadyEvent, InternalEvent}

class ClientsCollection(autoWatchDog: Boolean) extends Logging {
    protected val clients: mutable.TreeMap[String, BoundMessengerClient] = new mutable.TreeMap[String, BoundMessengerClient]
    protected var watchDogThread: Thread = null
    
    if(autoWatchDog) {
        logger.info("Requested automatic watchdog start, starting...")
        watchDogThread = new Thread(new ClientWatchDogTask(this))
        watchDogThread.start()
    }
    
    def clientList: mutable.TreeMap[String, BoundMessengerClient] = clients.clone()
    
    def acquireClient(email: String, token: String): BoundMessengerClient = {
        val client: Option[BoundMessengerClient] = clients.get(email)
        if(client.isEmpty)
            throw new NoClientException(s"No online client for $email")
        else if(!client.get.token.equals(token))
            throw new InvalidAuthTokenException("Invalid token supplied")
        
        return client.get
    }
    
    def createClient(email: String, password: String): String = {
        logger.info(s"Received request for login: $email")
        
        val maybeClient: Option[BoundMessengerClient] = clients.get(email)
        if(maybeClient.isDefined) {
            if(maybeClient.get.dead) {
                logger.debug(s"$email already online, but it is dead, destroying...")
    
                maybeClient.get.kill()
                clients.remove(email)
            } else {
                if(maybeClient.get.checkPassword(password)) {
                    // This will not make clients universally reusable amongst different devices
                    // but it is meant for recovering existing session after connectivity issues
                    // without going through login process again.
                    // As such, it is assumed that there are no connections to this exact client except the current,
                    // so event for "readiness" is triggered once more.
                    // In general, relying on this mechanism in non-debug environments is very, very bad.
                    // The behaviour is undefined, just keep track of your auth key and you'll be fine.
                    logger.debug(s"$email already online and up, passwords match, reusing...")
                    
                    maybeClient.get.enqueueEvent(new InternalEvent(101)) // Kill of existing connection
                    Thread.sleep(500)
                    
                    maybeClient.get.enqueueEvent(new ClientReadyEvent)
                    
                    return maybeClient.get.token
                } else {
                    logger.debug(s"Rejecting login request for $email: Password is known to be wrong")
    
                    throw new DuplicateClientException("Password is known to be wrong")
                }
            }
        }
    
        val emailAddr: Email = Email.parseStr(email)
        if(emailAddr == null) {
            logger.info(s"Rejecting login request for $email: Invalid address")
            
            throw new IllegalArgumentException("Email is incorrect")
        }
    
        val messenger: EscargotMessenger = new EscargotMessenger(emailAddr, password)
        messenger.setSupportedProtocol(Array[MsnProtocol](MsnProtocol.MSNP12))
        messenger.getOwner.setInitStatus(MsnUserStatus.ONLINE)
        
        if(System.getProperty("msn.debug", "0").equals("1")) {
            messenger.setLogIncoming(true)
            messenger.setLogOutgoing(true)
        }
        
        val client: BoundMessengerClient = new BoundMessengerClient(email, messenger, password)
        clients += ((email, client))
    
        logger.info(s"Created client for $email")
        
        return client.token
    }
    
    def killClient(email: String): Boolean = {
        logger.info(s"Received request to kill $email")
        
        val maybeClient: Option[BoundMessengerClient] = clients.get(email)
        if(maybeClient.isEmpty)
            return false
            
        maybeClient.get.kill()
        clients.remove(email)
        
        return true
    }
}