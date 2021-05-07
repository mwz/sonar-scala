scalaVersion := "2.12.11"
scalacOptions -= "-Wconf:cat=unused-nowarn:s"
libraryDependencies ++= Seq(
  "org.sonarsource.update-center" % "sonar-update-center-common" % "1.26.0.846",
  // Scapegoat & scalastyle inspections generator dependencies
  "com.sksamuel.scapegoat" % s"scalac-scapegoat-plugin_${scalaVersion.value}" % "1.4.8",
  "com.beautiful-scala"   %% "scalastyle"                                     % "1.5.0",
  "org.scalameta"         %% "scalameta"                                      % "4.4.16",
  "org.scalatest"         %% "scalatest"                                      % "3.2.8" % Test
)

// Adding a resolver to the Artima maven repo, so sbt can download the Artima SuperSafe sbt plugin
resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots"),
  "Artima Maven Repository" at "https://repo.artima.com/releases"
)

logBuffered in Test := false
testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-oDTF")
