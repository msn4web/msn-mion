package pl.jill.webmsn.MSN.Events
import io.circe.Json

class LoginFailedEvent extends SerializableEvent {
    def kind: String = "UNAUTHORIZED"
    
    def json: Json = Json.obj (
        ("authorized", Json.fromBoolean(false))
    )
}