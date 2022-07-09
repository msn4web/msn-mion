package pl.jill.webmsn.MSN.Events
import io.circe.Json

class ClientReadyEvent extends SerializableEvent {
    def kind: String = "READY"
    
    def json: Json = Json.obj (
        ("ready", Json.fromBoolean(true))
    )
}