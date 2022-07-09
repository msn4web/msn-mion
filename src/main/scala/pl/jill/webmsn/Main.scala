package pl.jill.webmsn

import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import pl.jill.webmsn.GatewayService.ClientsCollection
import pl.jill.webmsn.WebApi.WebApiServerFactory
import org.apache.logging.log4j.LogManager

object Main {
    def main(args: Array[String]): Unit = {
        val port: Int = System.getProperty("http.port", "1864").toInt
        val collection: ClientsCollection = new ClientsCollection(true)
        val sock: InetSocketAddress = new InetSocketAddress(System.getProperty("http.host", "localhost"), port)
        val server: HttpServer = WebApiServerFactory.createWebApiServer(sock, collection)
        
        server.start()
    }
}