package pl.jill.webmsn.WebApi.Methods

import io.circe.Json
import pl.jill.webmsn.MSN.BoundMessengerClient
import pl.jill.webmsn.MSN.Events.SerializableEvent

import scala.collection.mutable.ArrayBuffer

class GetUpdatesMethod(mess: BoundMessengerClient, params: Map[String, Json]) extends AbstractWebApiMethod(mess, params) {
    override def execute(): Json = {
        val startTime: Long = System.currentTimeMillis
        while(startTime - System.currentTimeMillis <= (30 * 1000)) {
            val events: ArrayBuffer[SerializableEvent] = mess.events
            
            if(events.nonEmpty) {
                var i: Int = 0;
                val jsEvents = new Array[Json](events.length)
                for(ev <- events) {
                    jsEvents(i) = Json.obj(
                        ("@type", Json.fromString(ev.kind)),
                        ("data", ev.json)
                    )
                    i += 1
                }
                
                return Json.obj(
                    ("updates", Json.arr(jsEvents:_*))
                )
            }
            
            // Sleep a bit to save cpu time
            Thread.sleep(100)
        }
        
        return Json.obj(
            ("updates", Json.arr())
        )
    }
}