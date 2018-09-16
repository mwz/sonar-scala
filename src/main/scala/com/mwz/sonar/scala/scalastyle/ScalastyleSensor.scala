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
package scalastyle

import java.io.File

import com.mwz.sonar.scala.util.Log
import org.scalastyle._
import org.sonar.api.batch.fs.{FilePredicates, InputFile}
import org.sonar.api.batch.rule.{ActiveRule, Severity}
import org.sonar.api.batch.sensor.issue.NewIssue
import org.sonar.api.batch.sensor.{Sensor, SensorContext, SensorDescriptor}
import org.sonar.api.profiles.{RulesProfile => QualityProfile}

import scala.collection.JavaConverters._
import scala.collection.immutable.Seq

/**
 * Main sensor for executing Scalastyle analysis.
 */
final class ScalastyleSensor(qualityProfile: QualityProfile) extends Sensor {
  private[this] val log = Log(classOf[ScalastyleSensor], "scalastyle")

  override def describe(descriptor: SensorDescriptor): Unit = {
    descriptor
      .createIssuesForRuleRepository(ScalastyleRulesRepository.RepositoryKey)
      .name(ScalastyleSensor.SensorName)
      .onlyOnFileType(InputFile.Type.MAIN)
      .onlyOnLanguage(Scala.LanguageKey)
  }

  override def execute(context: SensorContext): Unit = {
    log.info("Initializing the Scalastyle sensor.")

    val inspections: Map[String, ScalastyleInspection] =
      ScalastyleInspections.AllInspections.map(i => i.clazz -> i).toMap

    val activeRules: Seq[ActiveRule] = context
      .activeRules()
      .findByRepository(ScalastyleRulesRepository.RepositoryKey)
      .asScala
      .toIndexedSeq

    val checks: Map[String, Option[ConfigurationChecker]] =
      activeRules.map(r => r.ruleKey.rule -> ScalastyleSensor.ruleToConfigurationChecker(r)).toMap

    checks.filter { case (_, conf) => conf.isEmpty } foreach {
      case (ruleKey, _) =>
        log.warn(
          s"Rule $ruleKey is missing the ${ScalastyleRulesRepository.RuleClassParam} parameter " +
          "and it will be skipped during the analysis."
        )
    }

    val config: ScalastyleConfiguration = new ScalastyleConfiguration(
      "SonarQube",
      commentFilter = true,
      checks.collect { case (_, Some(conf)) => conf }.toList // unNone
    )

    val fileSpecs: Seq[FileSpec] = ScalastyleSensor.fileSpecs(context)

    val messages: Seq[Message[FileSpec]] = new ScalastyleChecker()
      .checkFiles(config, fileSpecs)

    messages foreach {
      // Process each Scalastyle result.
      case e: StyleError[_] =>
        process(context, inspections, e)
      case e: StyleException[_] =>
        log.error(s"Scalastyle exception (checker: ${e.clazz}, file: ${e.fileSpec.name}): ${e.message}.")
      case _ =>
        Unit
    }
  }

  def process(
    context: SensorContext,
    inspections: Map[String, ScalastyleInspection],
    styleError: StyleError[FileSpec]
  ): Unit = {
    log.debug(s"Processing ${styleError.clazz} for file ${styleError.fileSpec}.")
    val activeRule = Option(
      context
        .activeRules()
        .findByInternalKey(ScalastyleRulesRepository.RepositoryKey, styleError.key)
    )

    activeRule match {
      case Some(rule) =>
        val predicates = context.fileSystem.predicates
        val file: InputFile = context.fileSystem.inputFile(predicates.hasPath(styleError.fileSpec.name))
        val newIssue: NewIssue = context.newIssue().forRule(rule.ruleKey)
        val line: Int = styleError.lineNumber.filter(_ > 0).getOrElse(1) // scalastyle:ignore
        val message: Option[String] = styleError.customMessage orElse inspections
          .get(styleError.clazz.getName)
          .map(_.label)

        newIssue
          .at(
            newIssue
              .newLocation()
              .on(file)
              .at(file.selectLine(line))
              .message(message.getOrElse(""))
          )
          .save()
      case None =>
        log.warn(
          s"Scalastyle rule with key ${styleError.key} was not found" +
          s"in the ${qualityProfile.getName} quality profile."
        )
    }
  }
}

private[scalastyle] object ScalastyleSensor {
  final val SensorName = "Scalastyle Sensor"

  def ruleToConfigurationChecker(rule: ActiveRule): Option[ConfigurationChecker] = {
    val params = rule.params.asScala.map { case (k, v) => k -> v.trim }.toMap
    val className: Option[String] = params.get(ScalastyleRulesRepository.RuleClassParam).filter(_.nonEmpty)

    className.map { className =>
      ConfigurationChecker(
        className,
        ScalastyleRulesRepository.severityToLevel(Severity.valueOf(rule.severity)),
        enabled = true,
        params,
        customMessage = None,
        customId = Some(rule.ruleKey.rule)
      )
    }
  }

  def fileSpecs(context: SensorContext): Seq[FileSpec] = {
    val predicates: FilePredicates = context.fileSystem.predicates
    val files: Iterable[File] = context.fileSystem
      .inputFiles(
        predicates.and(
          predicates.hasLanguage(Scala.LanguageKey),
          predicates.hasType(InputFile.Type.MAIN)
        )
      )
      .asScala
      .map(f => new File(f.uri)) // Avoiding here to use InputFile.file, which is deprecated.

    Directory.getFiles(Some(context.fileSystem.encoding.name), files)
  }
}