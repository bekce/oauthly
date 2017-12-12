name := """oauthly"""
organization := "com.sebworks"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

resolvers += Resolver.mavenLocal

scalaVersion := "2.12.2"

libraryDependencies ++= Seq(
  guice,
  ws
)

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.sebworks.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.sebworks.binders._"

libraryDependencies ++= Seq(
//  "commons-io" % "commons-io" % "2.6",
  "org.mindrot"%"jbcrypt"%"0.4",
//  "commons-validator"%"commons-validator"%"1.4.1",
  "com.auth0"%"java-jwt"%"2.2.0",
  "uk.co.panaxiom" %% "play-jongo" % "2.0.0-jongo1.3"
)

// "org.mongodb.morphia" % "morphia" % "1.3.2",
// "org.mongodb" % "mongo-java-driver" % "3.2.2"
