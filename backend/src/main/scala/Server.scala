import cats.effect.{IO, IOApp}
import org.http4s.HttpRoutes
import org.http4s.dsl.io._
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Router
import org.http4s.implicits._
import com.comcast.ip4s.{Host, Port}
import org.http4s.server.staticcontent._

object Server extends IOApp.Simple {

  // API route: default page returns hello world
  val apiRoutes = HttpRoutes.of[IO] {
    case GET -> Root =>
      Ok("Hello, World!")
  }

  // Static file routes (serves files from backend/public)
  val staticRoutes = fileService[IO](FileService.Config("public", pathPrefix = ""))

  // Combine routes: default on "/" and static files (like index.html) under "/page/domain"
  val httpApp = Router[IO](
    "/"           -> apiRoutes,
    "/page/domain" -> staticRoutes
  ).orNotFound

  val port = sys.env.get("PORT").flatMap(p => scala.util.Try(p.toInt).toOption).getOrElse(8080)

  val run = EmberServerBuilder
    .default[IO]
    .withHost(Host.fromString("0.0.0.0").get)
    .withPort(Port.fromInt(port).get)
    .withHttpApp(httpApp)
    .build
    .use(_ => IO.println(s"Server running at http://0.0.0.0:$port") >> IO.never)
}
