package pl.jill.webmsn.WebApi.Handlers

import com.sun.net.httpserver.{HttpExchange, HttpHandler}
import pl.jill.webmsn.Util.StreamingJSONReader

import java.io.{InputStream, OutputStream}

class ApiDistributionHandler extends HttpHandler {
    override def handle(exchange: HttpExchange): Unit = {
        val outputStream: OutputStream = exchange.getResponseBody
        val inputStream: InputStream = Thread.currentThread()
          .getContextClassLoader.getResourceAsStream("api.js")
        val js: String = StreamingJSONReader.streamToString(inputStream)
        val binJs: Array[Byte] = js.getBytes("UTF-8")
        
        exchange.getResponseHeaders.set("Content-Type", "text/javascript")
        exchange.sendResponseHeaders(200, binJs.length)
        outputStream.write(binJs)
        outputStream.flush()
        outputStream.close()
    }
}
