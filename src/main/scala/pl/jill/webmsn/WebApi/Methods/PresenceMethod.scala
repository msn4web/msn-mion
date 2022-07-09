package pl.jill.webmsn.WebApi.Methods

import io.circe.Json
import net.sf.jml.MsnUserStatus
import pl.jill.webmsn.MSN.BoundMessengerClient

class PresenceMethod(mess: BoundMessengerClient, params: Map[String, Json]) extends AbstractWebApiMethod(mess, params) {
    override def execute(): Json = {
        if(!params.contains("status"))
            throw new MethodException("Status parameter must be set")
            
        val status: String = params("status").asString.getOrElse("undefined")
        client.client.getOwner.setStatus(status match {
            case "online"  => MsnUserStatus.ONLINE
            case "busy"    => MsnUserStatus.BUSY
            case "away"    => MsnUserStatus.AWAY
            case "brb"     => MsnUserStatus.BE_RIGHT_BACK
            case "lunch"   => MsnUserStatus.OUT_TO_LUNCH
            case "idle"    => MsnUserStatus.IDLE
            case "phone"   => MsnUserStatus.ON_THE_PHONE
            case "offline" => MsnUserStatus.HIDE
            
            case _ => throw new MethodException(s"Invalid status $status")
        })
        
        return Json.obj(
            ("presence", Json.fromString(status))
        )
    }
}