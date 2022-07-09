package pl.jill.webmsn.WebApi

import pl.jill.webmsn.GatewayService.ClientsCollection
import pl.jill.webmsn.MSN.BoundMessengerClient
import java.nio.charset.StandardCharsets
import java.util.Base64

object ClientAuthenticator {
    def getAuthPair(str: String): Option[(String, String)] = {
        val encoded = str.getBytes("UTF-8")
        val decoded = new String(Base64.getDecoder.decode(encoded), StandardCharsets.UTF_8)
        
        val pair: Array[String] = decoded.split(":")
        if(pair.length != 2)
            return None
        
        return Some((pair(0), pair(1)))
    }
    
    def authorizeByKey(key: String, collection: ClientsCollection): Option[BoundMessengerClient] = {
        val maybeAuthPair = getAuthPair(key)
        if(maybeAuthPair.isEmpty)
            return None
    
        val authPair = maybeAuthPair.get
        try {
            return Some(collection.acquireClient(authPair._1, authPair._2))
        } catch {
            case ex: Any => return None
        }
    }
}