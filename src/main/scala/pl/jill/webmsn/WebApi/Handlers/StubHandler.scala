package pl.jill.webmsn.WebApi.Handlers

import com.sun.net.httpserver.{HttpExchange, HttpHandler}
import pl.jill.webmsn.GatewayService.ClientsCollection

import java.io.OutputStream

class StubHandler(colle: ClientsCollection) extends HttpHandler {
    protected val clients: ClientsCollection = colle
    
    override def handle(exchange: HttpExchange): Unit = {
        val outputStream: OutputStream = exchange.getResponseBody
        val htmlBuilder: StringBuilder = new StringBuilder
    
        htmlBuilder.append("<center>")
          .append("<h1>escargot2https</h1>")
          .append("<hr/>")
          .append("There are <b>")
          .append(clients.clientList.size.toString)
          .append("</b> clients online!");
        
        val html: String = htmlBuilder.toString()
        exchange.getResponseHeaders.set("Content-Type", "text/html")
        exchange.sendResponseHeaders(202, html.length)
        outputStream.write(html.getBytes("UTF-8"))
        outputStream.flush()
        outputStream.close()
    }
}