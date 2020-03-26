package es.richweb.markdownmanager

import org.http4s.client._
import org.http4s.client.dsl.io._
import org.http4s.headers._
import org.http4s.{BasicCredentials, MediaType, Request, Uri, UrlForm}
import org.http4s.Method._
import org.http4s.circe._
import cats.effect.IO
import io.circe.Json
import io.circe.syntax._

object Spaces {

  val uri = Uri
    .unsafeFromString(Main.baseurl)
    .withPath("/rest/api/content/56000689")
    .withQueryParams(
      Map(
        "type" -> "page",
        "spaceKey" -> "DOC",
        "expand" -> "body.storage"
      )
    )

  val request = GET(
    uri,
    Authorization(new BasicCredentials(Main.user, Main.pass)),
    Accept(MediaType.application.json)
  )

  val postUri = Uri
    .unsafeFromString(Main.baseurl)
    .withPath("/rest/api/content")

  val postRequest: (String, String) => IO[Request[IO]] = (title: String, body: String) => POST(
    Json.obj(
      ("type", Json.fromString("page")),
      ("title", Json.fromString(title)),
      ("space",
        Json.obj(
          ("key", Json.fromString("DOC"))
        )
      ),
      ("body",
        Json.obj(
          ("storage",
            Json.obj(
              ("value", Json.fromString(body)),
             ("representation", Json.fromString("storage"))
            )
          )
        )
      )
    ),
    postUri,
    Authorization(new BasicCredentials(Main.user, Main.pass)),
    Accept(MediaType.application.json)
  )

  def get(client: Client[IO]): IO[Json] = client.expect[Json](request).handleErrorWith(t => IO.pure(t.getMessage.asJson))

  def post(client: Client[IO], ioJson: IO[Json]): IO[Json] = ioJson.flatMap(json => {
    val cursor = json.hcursor
    val title  = cursor.downField("mdname").as[String].fold[String](_ => "", s => s)
    val body   = cursor.downField("typed").as[String].fold[String](_ => "", s => s)
    client.expect[Json](postRequest(title, body)).map(rq => {
      val resp = rq.as[Json].fold(df => df.message.asJson, j => j)
      println(resp.noSpaces)
      resp
    }).handleErrorWith(t => IO.pure(t.getMessage.asJson))
  })

}
