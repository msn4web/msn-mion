package pl.jill.webmsn.WebApi.Methods

import io.circe.Json

trait WebApiMethod {
    def execute(): Json
}