package es.richweb.markdownmanager

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._

object Main extends IOApp {
  def run(args: List[String]) =
    Markdownmanagerhttp4sServer.stream[IO].compile.drain.as(ExitCode.Success)
}