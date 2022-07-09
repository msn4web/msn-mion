package pl.jill.webmsn.WebApi.Handlers

import com.sun.net.httpserver.HttpExchange
import io.circe.Json
import pl.jill.webmsn.GatewayService.ClientsCollection
import pl.jill.webmsn.WebApi.Methods._
import scala.collection.mutable

class ApiHandler(clients: ClientsCollection) extends AbstractHandler(clients) {
    protected var methods: mutable.TreeMap[String, AbstractWebApiMethod] = new mutable.TreeMap[String, AbstractWebApiMethod]
    
    protected def initMethods(): Unit = {
        methods += (("getMe", new GetMeMethod(client.get, params.get)))
        methods += (("getUpdates", new GetUpdatesMethod(client.get, params.get)))
        methods += (("sendMessage", new SendMessageMethod(client.get, params.get)))
        methods += (("contacts", new FetchContactsMethod(client.get, params.get)))
        methods += (("contact", new ContactMethod(client.get, params.get)))
        methods += (("presence", new PresenceMethod(client.get, params.get)))
    }
    
    override def run(exchange: HttpExchange): Unit = {
        if(params.isEmpty)
            return throwError(exchange, 400, "JSON object is required (even if method does not accept params)")
        
        initMethods()
        
        val path: Array[String] = exchange.getRequestURI.getPath.split("/")
        if(path.length != 3)
            return throwError(exchange, 404, "Not found")
        
        client.get.touch()
        val method: String = path(2) // should be right after "method"
        if(method.equals("logOut")) {
            collection.killClient(client.get.email)
            flushResponse(exchange, 200, Json.obj(("loggedOut", Json.fromBoolean(true))))
            return
        } else if(method.equals("noOp")) {
            flushResponse(exchange, 200, Json.obj(("time", Json.fromLong(System.currentTimeMillis))))
            return
        }
        
        val maybeHandler: Option[AbstractWebApiMethod] = methods.get(method)
        if(maybeHandler.isEmpty)
            return throwError(exchange, 404, s"Method $method is not found")
        
        val handler: AbstractWebApiMethod = maybeHandler.get
    
        var response: Json = null
        try {
            response = handler.execute()
        } catch {
            case ex: MethodException => return throwError(exchange, 400, ex.getMessage)
            case ex: Any => return throwError(exchange, 500, s"Internal server error: ${ex.getMessage}")
        }
        
        flushResponse(exchange, 200, response)
    }
}