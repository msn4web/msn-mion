package pl.jill.webmsn.MSN

import net.sf.jml.{MsnContact, MsnMessenger, MsnSwitchboard}
import net.sf.jml.event.MsnAdapter
import net.sf.jml.exception.IncorrectPasswordException
import net.sf.jml.message.{MsnControlMessage, MsnInstantMessage}
import org.apache.logging.log4j.scala.Logging
import pl.jill.webmsn.MSN.Events._

class BoundEventListener(boundMessengerClient: BoundMessengerClient) extends MsnAdapter with Logging {
    protected val client: BoundMessengerClient = boundMessengerClient
    
    override def exceptionCaught(messenger: MsnMessenger, ex: Throwable): Unit = {
        if(ex.isInstanceOf[IncorrectPasswordException]) {
            loginFailed()
            return
        }
    
        client.enqueueEvent(new ExceptionEvent(ex.getMessage))
    }
    
    def loginFailed(): Unit = {
        logger.info(s"Login failed for ${client.email}")
        
        client.enqueueEvent(new LoginFailedEvent)
        client.markAsDead()
    }
    
    override def loginCompleted(messenger: MsnMessenger): Unit = {
        logger.trace(s"Login succeeded for ${client.email}")
        client.markAsAlive()
        // not sending ClientReady here, waiting for contact list to initialize
    }
    
    override def instantMessageReceived(sb: MsnSwitchboard, mes: MsnInstantMessage, con: MsnContact): Unit = {
        val imEvent: InstantMessageEvent = new InstantMessageEvent(mes, con)
        client.enqueueEvent(imEvent)
    }
    
    override def controlMessageReceived(sb: MsnSwitchboard, mes: MsnControlMessage, con: MsnContact): Unit = {
         if(mes.getTypingUser != null)
             client.enqueueEvent(new UserTypingEvent(mes.getTypingUser, con))
    }
    
    override def contactStatusChanged(messenger: MsnMessenger, con: MsnContact): Unit = {
        client.enqueueEvent(new PresenceUpdatedEvent(con.getStatus, con))
    }
    
    override def contactAddedMe(messenger: MsnMessenger, contact: MsnContact): Unit = {
        client.enqueueEvent(new FriendRequestEvent(contact))
    }
    
    override def contactRemovedMe(messenger: MsnMessenger, contact: MsnContact): Unit = {
        client.enqueueEvent(new UnFriendEvent(contact))
    }
    
    override def contactListInitCompleted(messenger: MsnMessenger): Unit = {
        client.enqueueEvent(new ClientReadyEvent)
    }
    
    override def contactListSyncCompleted(messenger: MsnMessenger): Unit = {
        //logger.debug("cum sync")
    }
}