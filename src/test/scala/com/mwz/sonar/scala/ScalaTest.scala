package com.mwz.sonar.scala

import java.nio.file.Paths

import org.scalatest.{FlatSpec, Matchers}
import org.sonar.api.config.internal.MapSettings

class ScalaTest extends FlatSpec with Matchers {
  "getScalaVersion" should "return the available version, if properly set" in {
    val conf = new MapSettings()
      .setProperty("sonar.scala.version", "2.11.11")
      .asConfig()

    Scala.getScalaVersion(conf).toString shouldBe "2.11.11"
  }

  it should "return the default version, if the property is not set" in {
    val conf = new MapSettings().asConfig()
    Scala.getScalaVersion(conf).toString shouldBe "2.11.0"
  }

  it should "be able to parse a milestone version" in {
    val conf = new MapSettings()
      .setProperty("sonar.scala.version", "2.13.0-M3")
      .asConfig()

    Scala.getScalaVersion(conf).toString shouldBe "2.13.0-M3"
  }

  it should "be able to parse a version with an empty patch" in {
    val conf = new MapSettings()
      .setProperty("sonar.scala.version", "2.12.")
      .asConfig()

    Scala.getScalaVersion(conf).toString shouldBe "2.12."
  }

  it should "be able to parse a version without patch" in {
    val conf = new MapSettings()
      .setProperty("sonar.scala.version", "2.12")
      .asConfig()

    Scala.getScalaVersion(conf).toString shouldBe "2.12"
  }

  it should "return the default version, if the version property only contains the major version" in {
    val conf = new MapSettings()
      .setProperty("sonar.scala.version", "2")
      .asConfig()

    Scala.getScalaVersion(conf).toString shouldBe "2.11.0"
  }

  "getSourcesPaths" should "return the available sources" in {
    val conf1 = new MapSettings()
      .setProperty("sonar.sources", "sources/directory")
      .asConfig()

    Scala.getSourcesPaths(conf1) shouldBe List(Paths.get("sources/directory"))

    val conf2 = new MapSettings()
      .setProperty("sonar.sources", " sources/directory,  src/2 ")
      .asConfig()

    Scala.getSourcesPaths(conf2) shouldBe List(
      Paths.get("sources/directory"),
      Paths.get("src/2")
    )
  }

  it should "return the default value if not set" in {
    val conf = new MapSettings().asConfig()
    Scala.getSourcesPaths(conf) shouldBe List(Paths.get("src/main/scala"))
  }
}
