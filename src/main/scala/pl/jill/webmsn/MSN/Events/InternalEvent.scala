package pl.jill.webmsn.MSN.Events
import io.circe.Json

class InternalEvent(ptr: Long) extends SerializableEvent {
    override def kind: String = "INTERNAL"
    
    override def json: Json = Json.obj(
        ("ptr", Json.fromLong(ptr))
    )
}