package pl.jill.webmsn.WebApi

import pl.jill.webmsn.GatewayService.ClientsCollection
import com.sun.net.httpserver.{Authenticator, HttpExchange, HttpPrincipal}
import Authenticator._
import org.apache.logging.log4j.scala.Logging

class WebApiAuthenticator(collection: ClientsCollection) extends Authenticator with Logging {
    protected val clients: ClientsCollection = collection
    
    override def authenticate(exch: HttpExchange): Authenticator.Result = {
        if(exch.getRequestMethod.equals("OPTIONS"))
            return new Success(new HttpPrincipal("anonymous", "WebMSN API"))
        
        val authHeader = exch.getRequestHeaders.get("Authorization")
        if(authHeader == null)
            return new Failure(401)
            
        val authString  = authHeader.get(0).substring(6)
        val maybeClient = ClientAuthenticator.authorizeByKey(authString, clients)
        if(maybeClient.isEmpty) {
            logger.debug(s"Rejecting API call: Invalid auth")
            
            return new Retry(401)
        }
    
        return new Success(new HttpPrincipal(maybeClient.get.email, "WebMSN API"))
    }
}
