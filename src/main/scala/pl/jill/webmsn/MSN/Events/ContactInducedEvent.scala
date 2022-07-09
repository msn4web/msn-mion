package pl.jill.webmsn.MSN.Events

import net.sf.jml.MsnContact
import io.circe.{Encoder, Json}, io.circe.syntax._

abstract class ContactInducedEvent(contact: MsnContact) extends SerializableEvent {
    val user: MsnContact = contact
    
    protected def contactJson: Json = {
        implicit val encodeContact: Encoder[MsnContact] = (a: MsnContact) => Json.obj(
            ("id", Json.fromString(a.getId)),
            ("email", Json.fromString(a.getEmail.toString)),
            ("name", Json.fromString(a.getFriendlyName))
        )
        
        
        return user.asJson
    }
}