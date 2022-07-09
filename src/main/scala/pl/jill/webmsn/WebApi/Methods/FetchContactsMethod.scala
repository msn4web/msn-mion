package pl.jill.webmsn.WebApi.Methods

import io.circe.Json
import net.sf.jml.MsnContact
import pl.jill.webmsn.MSN.BoundMessengerClient

class FetchContactsMethod(mess: BoundMessengerClient, params: Map[String, Json]) extends AbstractWebApiMethod(mess, params) {
    override def execute(): Json = {
        var i: Int = 0
        val contacts: Array[MsnContact] = client.client.getContactList.getContacts
        val jsContacts: Array[Json] = new Array[Json](contacts.length)
        for(contact <- contacts) {
            jsContacts(i) = Json.obj(
                ("guid", Json.fromString(contact.getId)),
                ("email", Json.fromString(contact.getEmail.toString)),
                ("name", Json.fromString(contact.getFriendlyName)),
                ("status", Json.fromString(contact.getStatus.toString))
            )
            
            i += 1
        }
        
        return Json.obj(
            ("contacts", Json.arr(jsContacts:_*))
        )
    }
}