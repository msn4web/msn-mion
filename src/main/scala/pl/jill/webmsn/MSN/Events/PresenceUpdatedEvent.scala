package pl.jill.webmsn.MSN.Events

import io.circe.Json
import net.sf.jml.{MsnContact, MsnUserStatus}

class PresenceUpdatedEvent(status: MsnUserStatus, contact: MsnContact) extends ContactInducedEvent(contact) {
    override def kind: String = "PRESENCE"
    
    override def json: Json = Json.obj(
        ("contact", contactJson),
        ("status", Json.fromString(status.toString))
    )
}