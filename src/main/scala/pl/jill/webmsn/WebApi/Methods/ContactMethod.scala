package pl.jill.webmsn.WebApi.Methods

import io.circe.Json
import net.sf.jml.{Email, MsnContact}
import pl.jill.webmsn.MSN.BoundMessengerClient
import java.io.OutputStream

class ContactMethod(mess: BoundMessengerClient, params: Map[String, Json]) extends AbstractWebApiMethod(mess, params) {
    private def getContact(email: Email): Json = {
        val contact: MsnContact = client.client.getContactList.getContactByEmail(email)
        if(contact == null)
            throw new MethodException("There is no contact identified by this email in your roaster")
            
        return Json.obj(
            ("guid", Json.fromString(contact.getId)),
            ("email", Json.fromString(contact.getEmail.toString)),
            ("name", Json.fromString(contact.getFriendlyName)),
            ("status", Json.fromString(contact.getStatus.toString))
        )
    }
    
    def remContact(email: Email, block: Boolean): Json = {
        client.client.removeFriend(email, block)
        
        return Json.obj()
    }
    
    def addContact(email: Email, name: String): Json = {
        client.client.addFriend(email, name)
        
        return Json.obj()
    }
    
    override def execute(): Json = {
        if(!(params.contains("email") && params.contains("act")))
            throw new MethodException("Both email and act params must be present")
            
        val email: Email = Email.parseStr(params("email").asString.getOrElse(""))
        if(email == null)
            throw new MethodException("Malformed email")
            
        val act: String = params("act").asString.getOrElse("undefined")
        if(act.equals("add") && !params.contains("name"))
            throw new MethodException("Name (or false) must be specified in order to add person to roster")
        
        return act match {
            case "fetch"  => getContact(email)
            case "remove" => remContact(email, params.getOrElse("block", Json.fromBoolean(false)).asBoolean.getOrElse(false))
            case "add"    => addContact(email, params("name").asString.getOrElse(email.toString))
            case _        => throw new MethodException(s"Unknown act $act")
        }
    }
}