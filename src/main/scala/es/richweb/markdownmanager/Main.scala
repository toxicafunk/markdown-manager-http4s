package es.richweb.markdownmanager

import cats.effect._
import cats.implicits._
import io.circe.Json
import org.http4s.HttpRoutes
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.server.blaze._
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.circe._
import org.http4s.client.blaze._
import org.http4s.server.middleware._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.global

object Main extends IOApp {

  val add10: Int => Int = i => i + 10
  def times2: Int => Int = i => i * 2

  def add10ThenTimes2(i: Int): Int = times2(add10(i))

  val addThenMult: Int => Int = times2 compose add10

  // OptionT[IO, Request, Response]
  // Kleisli[Option[IO], Request, Response]

  case class Hello(name: String)

  private[markdownmanager] val baseurl = "https://confluence.eniro.com"
  private[markdownmanager] val user = "user"
  private[markdownmanager] val pass = "password"

  val services = HttpRoutes
    .of[IO] {
      case GET -> Root / "hello" / name =>
        Ok(Hello(s"hello $name").asJson)

      case GET -> Root / "spaces" =>
        BlazeClientBuilder[IO](global).resource.use { client =>
          Ok(Spaces.get(client))
        }

      case req @ POST -> Root / "create" =>
        BlazeClientBuilder[IO](global).resource.use { client =>
          val ioJson = req.as[Json]
          Ok(Spaces.post(client, ioJson))
        }
    }

  val methodConfig = CORSConfig(
    anyOrigin = false,
    allowedOrigins = Set("http://localhost:3000"),
    allowCredentials = false,
    anyMethod = false,
    allowedMethods = Some(Set("GET", "POST")),
    maxAge = 3.day.toSeconds)

  val corsService = CORS(services, methodConfig).orNotFound

  def run(args: List[String]): IO[ExitCode] =
    BlazeServerBuilder[IO]
      .bindHttp(8080, "localhost")
      .withHttpApp(corsService)
      .serve
      .compile
      .drain
      .as(ExitCode.Success)
}
