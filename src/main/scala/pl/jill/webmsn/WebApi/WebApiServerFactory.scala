package pl.jill.webmsn.WebApi

import com.sun.net.httpserver.HttpServer
import pl.jill.webmsn.GatewayService.ClientsCollection
import pl.jill.webmsn.WebApi.Handlers._
import java.net.InetSocketAddress
import java.util.concurrent.Executors

object WebApiServerFactory {
    def createWebApiServer(sock: InetSocketAddress, collection: ClientsCollection): HttpServer = {
        val minThreads: Int = System.getProperty("http.threads", "30").toInt
        val backLogging: Int = System.getProperty("http.backlogging", "0").toInt
        val httpServer: HttpServer = HttpServer.create(sock, backLogging)
    
        httpServer.setExecutor(Executors.newWorkStealingPool(minThreads))
        httpServer.createContext("/", new StubHandler(collection))
        httpServer.createContext("/MsnClient.js", new ApiDistributionHandler)
        httpServer.createContext("/acquireSession", new LoginHandler(collection))
        httpServer.createContext("/method", new ApiHandler(collection))
          .setAuthenticator(new WebApiAuthenticator(collection))
        
        return httpServer
    }
}