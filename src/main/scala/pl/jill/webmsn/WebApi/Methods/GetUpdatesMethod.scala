package pl.jill.webmsn.WebApi.Methods

import io.circe.Json
import pl.jill.webmsn.MSN.BoundMessengerClient

class GetUpdatesMethod(mess: BoundMessengerClient, params: Map[String, Json]) extends AbstractWebApiMethod(mess, params) {
    override def execute(): Json = {
        val startTime: Long = System.currentTimeMillis
        while(startTime - System.currentTimeMillis <= (30 * 1000)) {
            if(mess.eventCount > 0) {
                var i: Int = 0;
                val events = new Array[Json](mess.eventCount)
                for(ev <- mess.events) {
                    events(i) = Json.obj(
                        ("@type", Json.fromString(ev.kind)),
                        ("data", ev.json)
                    )
                    i += 1
                }
                
                return Json.obj(
                    ("updates", Json.arr(events:_*))
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
