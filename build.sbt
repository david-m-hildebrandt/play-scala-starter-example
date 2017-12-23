name := """play-scala-starter-example"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

resolvers += Resolver.sonatypeRepo("snapshots")

scalaVersion := "2.12.3"

libraryDependencies += guice
//libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test
libraryDependencies += "org.json4s" %% "json4s-native" % "3.5.3"
libraryDependencies += "org.json4s" %% "json4s-jackson" % "3.5.3"
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2"
libraryDependencies += "org.apache.httpcomponents" % "httpclient" % "4.5.3"
libraryDependencies += "com.h2database" % "h2" % "1.4.196"
libraryDependencies += ws


disablePlugins(PlayLayoutPlugin)
PlayKeys.playMonitoredFiles ++= (sourceDirectories in (Compile, TwirlKeys.compileTemplates)).value
