name := "opentelemetry-test"

version := "0.1"

scalaVersion := "2.12.11"

// https://mvnrepository.com/artifact/io.opentelemetry/opentelemetry-sdk
libraryDependencies += "io.opentelemetry" % "opentelemetry-api" % "0.4.0"
libraryDependencies += "io.opentelemetry" % "opentelemetry-sdk" % "0.4.0"
libraryDependencies += "io.opentelemetry" % "opentelemetry-exporters-logging" % "0.4.0"
libraryDependencies += "io.opentelemetry" % "opentelemetry-exporters-inmemory" % "0.4.0"
libraryDependencies += "io.opentelemetry" % "opentelemetry-exporters-jaeger" % "0.4.0"
libraryDependencies += "io.grpc" % "grpc-protobuf" % "1.28.0"
libraryDependencies += "io.grpc" % "grpc-netty-shaded" % "1.28.0"
libraryDependencies += "org.typelevel" %% "cats-effect" % "2.0.0"
libraryDependencies += "org.typelevel" %% "cats-core" % "2.0.0"








