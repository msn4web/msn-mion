package pl.jill.webmsn.WebApi.Handlers

import com.sun.net.httpserver.{HttpExchange, HttpHandler}
import io.circe.{Json, JsonObject}
import pl.jill.webmsn.GatewayService.ClientsCollection
import pl.jill.webmsn.MSN.BoundMessengerClient
import pl.jill.webmsn.Util.StreamingJSONReader
import pl.jill.webmsn.WebApi.ClientAuthenticator

import java.io.{InputStream, OutputStream}

abstract class AbstractHandler(colle: ClientsCollection) extends HttpHandler {
    protected val collection: ClientsCollection = colle
    protected var client: Option[BoundMessengerClient] = None
    protected var params: Option[Map[String, Json]] = None
    
    private def initParams(exchange: HttpExchange): Unit = {
        val body: InputStream = exchange.getRequestBody
        try {
            val maybeJson: Option[JsonObject] = StreamingJSONReader.deserializeStreamOrThrow(body).asObject
            if(maybeJson.isDefined) {
                val json: JsonObject = maybeJson.get
                params = Some(json.toMap)
            }
        } catch {
            case ex: Any => 1 + 1
        }
    }
    
    private def initClient(exchange: HttpExchange): Unit = {
        val authHeader = exchange.getRequestHeaders.get("Authorization")
        if(authHeader == null)
            return
        
        val authString  = authHeader.get(0).substring(6)
        client = ClientAuthenticator.authorizeByKey(authString, collection)
    }
    
    private def handlePreflight(exchange: HttpExchange): Unit = {
        exchange.getResponseHeaders.set("Access-Control-Allow-Methods", "POST")
        exchange.getResponseHeaders.set("Access-Control-Allow-Headers", "Authorization, Content-Type")
        exchange.getResponseHeaders.set("Access-Control-Max-Age", "86400")
        exchange.sendResponseHeaders(204, -1)
    }
    
    protected def flushResponse(exchange: HttpExchange, code: Int, json: Json): Unit = {
        val outputStream: OutputStream = exchange.getResponseBody
        val response: String = json.toString()
        val binResponse: Array[Byte] = response.getBytes("UTF-8")
    
        exchange.getResponseHeaders.set("Content-Type", "application/json")
        exchange.sendResponseHeaders(code, binResponse.length)
        outputStream.write(binResponse)
        outputStream.flush()
        outputStream.close()
    }
    
    protected def throwError(exchange: HttpExchange, code: Int, message: String): Unit = flushResponse(exchange, code, Json.obj(
        ("error", Json.fromBoolean(true)),
        ("details", Json.fromString(message))
    ))
    
    protected def getParam(key: String): Option[Json] = params.get.get(key)
    
    def run(exchange: HttpExchange): Unit;
    
    override def handle(exchange: HttpExchange): Unit = {
        val origin: String = exchange.getRequestHeaders.get("Origin").get(0)
        exchange.getResponseHeaders.set("Access-Control-Allow-Origin", origin)
        
        if(exchange.getRequestMethod == "OPTIONS") {
            handlePreflight(exchange)
            return
        }
        
        initParams(exchange)
        initClient(exchange)
        run(exchange)
    }
}
