package pl.jill.webmsn.WebApi.Handlers

import com.sun.net.httpserver.HttpExchange
import io.circe.Json
import pl.jill.webmsn.GatewayService.ClientsCollection
import pl.jill.webmsn.GatewayService.Exceptions.DuplicateClientException

class LoginHandler(clients: ClientsCollection) extends AbstractHandler(clients) {
    override def run(exchange: HttpExchange): Unit = {
        if(params.isEmpty || !(params.get.contains("email") && params.get.contains("password")))
            return throwError(exchange, 400, "Either email or password is empty (or both)")
    
        var token: String = ""
        try {
            token = clients.createClient(getParam("email").get.asString.get, getParam("password").get.asString.get)
        } catch {
            case ex: DuplicateClientException => return throwError(exchange, 400, "Session for this client already exists")
            case ex: IllegalArgumentException => return throwError(exchange, 400, "Email is malformed")
        }
        
        flushResponse(exchange, 200, Json.obj(
            ("key", Json.fromString(token))
        ))
    }
}
