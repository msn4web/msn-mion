package pl.jill.webmsn.WebApi.Methods

import io.circe.Json
import pl.jill.webmsn.MSN.BoundMessengerClient

abstract class AbstractWebApiMethod(mess: BoundMessengerClient, params: Map[String, Json]) {
    protected val client: BoundMessengerClient = mess
    protected val options: Map[String, Json] = params
    
    def execute(): Json
}