package pl.jill.webmsn.MSN.Events

import net.sf.jml.MsnContact
import net.sf.jml.message.MsnInstantMessage
import io.circe.{Encoder, Json}, io.circe.syntax._

class InstantMessageEvent(mess: MsnInstantMessage, contact: MsnContact) extends ContactInducedEvent(contact) {
    protected val message: MsnInstantMessage = mess
    
    def kind: String = "IM"
    
    def json: Json = {
        implicit val encodeIM: Encoder[MsnInstantMessage] = (a: MsnInstantMessage) => Json.obj(
            ("format", Json.obj(
                ("bold", Json.fromBoolean(a.isBold)),
                ("italic", Json.fromBoolean(a.isItalic)),
                ("underline", Json.fromBoolean(a.isUnderline)),
                ("strikethrough", Json.fromBoolean(a.isStrikethrough)),
                ("right", Json.fromBoolean(a.isRightAlign)),
                ("colour", Json.fromInt(a.getFontRGBColor)),
                ("font", Json.fromString(a.getFontName))
            )),
    
            ("contact", contactJson),
            ("text", Json.fromString(a.getContent))
        )
        
        return message.asJson
    }
}