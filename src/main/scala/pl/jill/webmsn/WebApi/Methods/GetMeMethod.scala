package pl.jill.webmsn.WebApi.Methods

import io.circe.Json
import pl.jill.webmsn.MSN.BoundMessengerClient

class GetMeMethod(mess: BoundMessengerClient, params: Map[String, Json]) extends AbstractWebApiMethod(mess, params) {
    override def execute(): Json = Json.obj(
        ("email", Json.fromString(client.email)),
        ("isDead", Json.fromBoolean(client.dead)),
        ("pendingEvents", Json.fromInt(client.eventCount))
    )
}