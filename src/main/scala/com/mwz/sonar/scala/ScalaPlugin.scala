/*
 * Sonar Scala Plugin
 * Copyright (C) 2018 All contributors
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package com.mwz.sonar.scala

import java.nio.file.{Path, Paths}

import cats.kernel.Eq
import com.mwz.sonar.scala.util.JavaOptionals._
import com.mwz.sonar.scala.util.Log
import org.sonar.api.Plugin
import org.sonar.api.config.Configuration
import org.sonar.api.resources.AbstractLanguage
import scalariform.ScalaVersion
import scalariform.lexer.{ScalaLexer, Token}
import scalariform.utils.Utils._

/** Defines Scala as a language for SonarQube */
final class Scala(settings: Configuration) extends AbstractLanguage(Scala.LanguageKey, Scala.LanguageName) {
  override def getFileSuffixes: Array[String] = {
    val suffixes: Array[String] = settings.getStringArray(Scala.FileSuffixesPropertyKey)
    val filtered: Array[String] = suffixes.filter(_.trim.nonEmpty)
    if (filtered.nonEmpty) filtered
    else Scala.DefaultFileSuffixes
  }
}

object Scala {
  val LanguageKey = "scala"
  val LanguageName = "Scala"

  private val FileSuffixesPropertyKey = "sonar.scala.file.suffixes"
  private val DefaultFileSuffixes = Array(".scala")
  private val ScalaVersionPropertyKey = "sonar.scala.version"
  private val DefaultScalaVersion = ScalaVersion(2, 12) // scalastyle:ignore org.scalastyle.scalariform.NamedArgumentChecker
  private val ScalaVersionPattern = """(\d+)\.(\d+)(?:\..+)?""".r
  private val SourcesPropertyKey = "sonar.sources"
  private val DefaultSourcesFolder = "src/main/scala"

  private val logger = Log(classOf[Scala], "sonar-scala")

  def getScalaVersion(settings: Configuration): ScalaVersion = {
    def parseVersion(s: String): Option[ScalaVersion] = s match {
      case ScalaVersionPattern(major, minor) =>
        for {
          major <- major.toIntOpt
          minor <- minor.toIntOpt
        } yield ScalaVersion(major, minor)
      case _ =>
        None
    }

    val scalaVersion: Option[ScalaVersion] =
      settings
        .get(ScalaVersionPropertyKey)
        .toOption
        .flatMap(parseVersion)

    // log a warning if the version is not set correctly or missing
    if (scalaVersion.isEmpty)
      logger.warn(
        s"The '$ScalaVersionPropertyKey' is not properly set or is missing, " +
        s"using the default value: '$DefaultScalaVersion'."
      )

    scalaVersion.getOrElse(DefaultScalaVersion)
  }

  // even if the 'sonar.sources' property is mandatory,
  // we add a default value to ensure a safe access to it
  def getSourcesPaths(settings: Configuration): List[Path] =
    settings
      .get(SourcesPropertyKey)
      .toOption
      .filter(_.nonEmpty)
      .getOrElse(DefaultSourcesFolder)
      .split(',') // scalastyle:ignore org.scalastyle.scalariform.NamedArgumentChecker
      .map(p => Paths.get(p.trim))
      .toList

  def tokenize(sourceCode: String, scalaVersion: ScalaVersion): List[Token] =
    ScalaLexer
      .createRawLexer(sourceCode, forgiveErrors = false, scalaVersion.toString)
      .toList
}

/** Sonar Scala plugin entry point */
final class ScalaPlugin extends Plugin {
  override def define(context: Plugin.Context): Unit = {
    context.addExtensions(
      // Scala.
      classOf[Scala],
      classOf[sensor.ScalaSensor],
      // Scalastyle.
      classOf[scalastyle.ScalastyleRulesRepository],
      classOf[scalastyle.ScalastyleQualityProfile],
      classOf[scalastyle.ScalastyleChecker],
      classOf[scalastyle.ScalastyleSensor],
      // Scapegoat.
      classOf[scapegoat.ScapegoatRulesRepository],
      classOf[scapegoat.ScapegoatQualityProfile],
      classOf[scapegoat.ScapegoatReportParser],
      classOf[scapegoat.ScapegoatSensor],
      // Built-in quality profiles.
      classOf[qualityprofiles.ScalastyleScapegoatQualityProfile],
      classOf[qualityprofiles.RecommendedQualityProfile],
      // Scoverage.
      classOf[scoverage.ScoverageMetrics],
      classOf[scoverage.ScoverageReportParser],
      classOf[scoverage.ScoverageSensor]
    )
  }
}
