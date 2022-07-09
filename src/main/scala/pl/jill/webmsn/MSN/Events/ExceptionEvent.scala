package pl.jill.webmsn.MSN.Events
import io.circe.Json

class ExceptionEvent(message: String) extends SerializableEvent {
    override def kind: String = "EXCEPTION"
    
    override def json: Json = Json.obj(
        ("message", Json.fromString(message))
    )
}