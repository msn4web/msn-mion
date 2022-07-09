package pl.jill.webmsn.MSN.Events

import io.circe.Json
import net.sf.jml.MsnContact

class UnFriendEvent(contact: MsnContact) extends ContactInducedEvent(contact) {
    override def kind: String = "UNFRIENDED"
    
    override def json: Json = Json.obj(
        ("user", contactJson)
    )
}
