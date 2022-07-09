package pl.jill.webmsn.WebApi.Methods

import io.circe.Json
import net.sf.jml.Email
import pl.jill.webmsn.MSN.BoundMessengerClient

class SendMessageMethod(mess: BoundMessengerClient, params: Map[String, Json]) extends AbstractWebApiMethod(mess, params) {
    override def execute(): Json = {
        if(!(params.contains("peer") && params.contains("text")))
            throw new MethodException("Both peer and text params must be present")
        
        val email: Email = Email.parseStr(params("peer").asString.getOrElse(""))
        if(email == null)
            throw new MethodException("Invalid email format")
        
        client.client.sendText(email, params("text").asString.getOrElse("[client sent a message WebMSN proxy couldn't parse]"))
        
        return Json.obj()
    }
}