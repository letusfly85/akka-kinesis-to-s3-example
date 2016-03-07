name := "akka-kinesis-to-s3-example"

organization := "org.triplew.example"

version := "1.0"

scalaVersion := "2.11.7"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")


libraryDependencies ++= {
  val akkaV       = "2.4.+"
  val akkaStreamV = "2.0.3"
  Seq(
    "com.typesafe.akka" % "akka-actor_2.11" % akkaV,
    "com.typesafe.akka" % "akka-stream-experimental_2.11" % akkaStreamV,
    "com.typesafe.akka" % "akka-http-core-experimental_2.11" % akkaStreamV,
    "com.typesafe.akka" % "akka-http-experimental_2.11" % akkaStreamV,
    "com.typesafe.akka" % "akka-http-spray-json-experimental_2.11" % akkaStreamV,
    "com.typesafe.akka" % "akka-http-testkit-experimental_2.11" % akkaStreamV,
    "org.specs2" % "specs2_2.11" % "3.7" % "test",
    "com.github.seratch" %% "awscala" % "0.5.+",
    "com.amazonaws" % "amazon-kinesis-client" % "1.6.1",
    "commons-configuration" % "commons-configuration" % "1.10"
  )
}


Revolver.settings

wartremoverErrors ++= Seq(
  Wart.IsInstanceOf,
  Wart.Return,
  Wart.Any2StringAdd,
  Wart.OptionPartial,
  Wart.TryPartial,
  Wart.ListOps,
  Wart.Null,
  Wart.Product,
  Wart.Serializable,
  Wart.Nothing,
  Wart.Var,
  //Wart.Throw,
  Wart.Enumeration,
  Wart.ToString,
  Wart.FinalCaseClass,
  Wart.EitherProjectionPartial,
  Wart.ExplicitImplicitTypes,
  Wart.AsInstanceOf)

lazy val util = (project in file("akka-kinesis-to-s3-example")).
  enablePlugins(JavaAppPackaging)
