package pl.jill.webmsn.MSN.Events

import io.circe.Json

trait SerializableEvent {
    def kind: String
    def json: Json
}