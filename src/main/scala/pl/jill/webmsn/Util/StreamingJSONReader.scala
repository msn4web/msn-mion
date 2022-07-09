package pl.jill.webmsn.Util

import io.circe._, io.circe.parser._

import java.io.{BufferedReader, InputStream, InputStreamReader}
import java.nio.charset.StandardCharsets
import java.util.stream.Collectors

object StreamingJSONReader {
    def streamToString(stream: InputStream): String = {
        new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))
          .lines()
          .collect(Collectors.joining("\r\n"))
    }
    
    def deserializeStream(stream: InputStream): Either[ParsingFailure, Json] = {
        val str: String = streamToString(stream)
        
        return parse(str)
    }
    
    def deserializeStreamOrThrow(stream: InputStream): Json = {
        val maybeJson = deserializeStream(stream)
        
        maybeJson match {
            case Left(failure) => throw new JSONParseException
            case Right(json)   => return json
        }
    }
}