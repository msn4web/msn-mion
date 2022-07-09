package pl.jill.webmsn.MSN.Events

import io.circe.Json
import net.sf.jml.MsnContact

class FriendRequestEvent(contact: MsnContact) extends ContactInducedEvent(contact) {
    override def kind: String = "FRIENDED"
    
    override def json: Json = Json.obj(
        ("user", contactJson)
    )
}
