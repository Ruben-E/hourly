name := "hourly"

version := "1.0"

scalaVersion := "2.12.3"

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.4.20"
libraryDependencies += "com.typesafe.akka" %% "akka-persistence" % "2.4.20"
libraryDependencies += "org.sangria-graphql" %% "sangria" % "1.3.0"
libraryDependencies += "com.typesafe.akka" %% "akka-http" % "10.0.10"
libraryDependencies += "com.typesafe.akka" %% "akka-http-spray-json" % "10.0.10"
libraryDependencies += "org.sangria-graphql" %% "sangria-spray-json" % "1.0.0"
libraryDependencies += "com.github.dnvriend" %% "akka-persistence-inmemory" % "2.4.18.1"
