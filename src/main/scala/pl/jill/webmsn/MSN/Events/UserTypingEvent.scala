package pl.jill.webmsn.MSN.Events

import io.circe.Json
import net.sf.jml.MsnContact

class UserTypingEvent(email: String, contact: MsnContact) extends ContactInducedEvent(contact) {
    override def kind: String = "TYPING"
    
    override def json: Json = Json.obj(
        ("chat", contactJson),
        ("email", Json.fromString(email))
    )
}