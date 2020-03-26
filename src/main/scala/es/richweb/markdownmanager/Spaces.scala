package es.richweb.markdownmanager

import org.http4s.client._
import org.http4s.client.dsl.io._
import org.http4s.headers._
import org.http4s.MediaType
import org.http4s.Method._
import org.http4s.Uri
import org.http4s.Credentials
import org.http4s.AuthScheme
import org.http4s.circe._
import cats.effect.IO
import io.circe.Json

object Spaces {

  val request = GET(
    Uri.unsafeFromString(Manager.baseurl),
    Authorization(
      Credentials.Token(AuthScheme.Basic, s"${Manager.user} ${Manager.pass}")
    ),
    Accept(MediaType.application.json)
  )

  def get(client: Client[IO]) = client.expect[Json](request)

}
