import cats.data.{Kleisli}
import io.opentelemetry.exporters.jaeger.JaegerGrpcSpanExporter
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.trace.TracerSdkProvider
import io.opentelemetry.sdk.trace.export.SimpleSpansProcessor
import io.opentelemetry.trace.{Span, Tracer}
import cats.effect.{ExitCode, IO, IOApp, Resource}
import cats.implicits._
import scala.util.Try
import cats.effect.Sync

/**
  * @author Ayush Mittal
  */

object Example1 extends App {

  def trace[A](name: String)(doWork: => A): A = {
    val span =
      tracer.spanBuilder(name).setParent(tracer.getCurrentSpan).startSpan
    val scope = tracer.withSpan(span)
    val result = Try(doWork)
    scope.close()
    span.end()
    result.get
  }

  def rootMethod = trace("root") {
    methodA
  }

  def methodA = trace("A") {
    methodB + 5
  }

  def methodB = trace("B") {
    42
  }

  val tracerProvider: TracerSdkProvider = OpenTelemetrySdk.getTracerProvider
  tracerProvider.addSpanProcessor(
    SimpleSpansProcessor.create(
      JaegerGrpcSpanExporter.Builder.fromEnv
        .setServiceName("open-telemetry")
        .build))

  val tracer: Tracer = tracerProvider.get("com.ayushworks.telemetry-test")

  rootMethod
}

object Example2 extends IOApp {

  def trace[F[_]: Sync, A](name: String,
                           parentSpan: Span): Resource[F, Span] = {
    Resource.make {
      Sync[F].delay(tracer.spanBuilder(name).setParent(parentSpan).startSpan)
    } { span =>
      Sync[F].delay(span.end)
    }
  }

  type User = String

  def sendEmail[F[_]: Sync](user: User): F[Unit] = Sync[F].delay(())

  def dbSave[F[_]: Sync](user: User): F[User] = Sync[F].delay("user")

  def saveUser[F[_]: Sync](user: User, parentSpan: Span): F[User] =
    trace("save user", parentSpan).use { _ =>
      Thread.sleep(1000)
      for {
        savedUser <- dbSave(user)
        _ <- sendEmail(savedUser)
      } yield savedUser
    }

  def notify[F[_]: Sync](user: User, parentSpan: Span): F[Unit] =
    trace("notify", parentSpan).use {
      _ =>
        Thread.sleep(500)
      Sync[F].delay(())
    }

  def registerUser[F[_]: Sync](user: User, parentSpan: Span): F[User] =
    trace("register user", parentSpan).use { span =>
      for {
        result <- saveUser(user, span)
        _ <- notify(result, span)
      } yield result
    }

  val tracerProvider: TracerSdkProvider = OpenTelemetrySdk.getTracerProvider
  tracerProvider.addSpanProcessor(
    SimpleSpansProcessor.create(
      JaegerGrpcSpanExporter.Builder.fromEnv
        .setServiceName("open-telemetry")
        .build))

  val tracer: Tracer = tracerProvider.get("com.ayushworks.telemetry-test")

  val rootSpan: Span = tracer.spanBuilder("root").startSpan()

  override def run(args: List[String]): IO[ExitCode] = registerUser[IO]("user", rootSpan).flatTap(_ => IO(rootSpan.end())).as(ExitCode.Success)
}

object Example3 extends IOApp {

  import cats.effect.Sync

  def startSpan[F[_]: Sync, A](name: String,
                           parentSpan: Span): Resource[F, Span] = {
    Resource.make {
      Sync[F].delay(tracer.spanBuilder(name).setParent(parentSpan).startSpan)
    } { span =>
      Sync[F].delay(span.end)
    }
  }

  type Trace[F[_], A] = Kleisli[F, Span, A]

  def trace[F[_]: Sync, A](name: String)(trace: Trace[F, A]): Trace[F, A] = {
    Kleisli(
      parentSpan =>
        startSpan(name, parentSpan).use{
          childSpan => trace.run(childSpan)
        }
    )
  }

  type User = String

  def sendEmail[F[_]: Sync](user: User): F[Unit] = Sync[F].delay(())

  def dbSave[F[_]: Sync](user: User): F[User] = Sync[F].delay("user")

  def saveUser[F[_]: Sync](user: User): Trace[F, User] =
    trace("save user"){
      Kleisli.liftF {
        Thread.sleep(1000)
        for {
          savedUser <- dbSave(user)
          _ <- sendEmail(savedUser)
        } yield savedUser
      }
    }

  def notify[F[_]: Sync](user: User): Trace[F, Unit] =
    trace("notify") {
      Kleisli.liftF{
        Thread.sleep(500)
        Sync[F].delay(())
      }
    }

  def registerUser[F[_]: Sync](user: User): Trace[F, User] =
    trace("register user"){
      for {
        result <- saveUser(user)
        _ <- notify(result)
      } yield result
    }

  val tracerProvider: TracerSdkProvider = OpenTelemetrySdk.getTracerProvider
  tracerProvider.addSpanProcessor(
    SimpleSpansProcessor.create(
      JaegerGrpcSpanExporter.Builder.fromEnv
        .setServiceName("open-telemetry")
        .build))

  val tracer: Tracer = tracerProvider.get("com.ayushworks.telemetry-test")

  val rootSpan: Span = tracer.spanBuilder("root").startSpan()

  override def run(args: List[String]): IO[ExitCode] = registerUser[IO]("user").run(rootSpan).flatTap(_ => IO(rootSpan.end())).as(ExitCode.Success)

}