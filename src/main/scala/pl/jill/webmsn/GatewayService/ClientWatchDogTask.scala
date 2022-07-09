package pl.jill.webmsn.GatewayService

import org.apache.logging.log4j.scala.Logging
import pl.jill.webmsn.MSN.BoundMessengerClient

class ClientWatchDogTask(colle: ClientsCollection) extends Runnable with Logging {
    protected val collection: ClientsCollection = colle
    
    protected def shouldBePerished(client: BoundMessengerClient): Boolean = {
        // Allow 1 minute grace period for new clients to authorize
        if(client.dead)
            return (System.currentTimeMillis - client.created) > (60 * 1000)
        
        // Kill all clients that are unused for more than 10 mins
        return (System.currentTimeMillis - client.updated) > (10 * 60 * 1000)
    }
    
    protected def hunt(): Unit = {
        for((email: String, client: BoundMessengerClient) <- collection.clientList) {
            if(shouldBePerished(client)) {
                logger.info(s"Killing ${client.email} due to inactivity")
                
                collection.killClient(email)
            }
        }
    }
    
    override def run(): Unit = {
        while(true) {
            logger.debug(s"Searching for dead or inactive clients to destroy")
            
            hunt()
            Thread.sleep(60 * 1000)
        }
    }
}