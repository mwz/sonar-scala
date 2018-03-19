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
package com.mwz.sonar.scala.scoverage

import com.mwz.sonar.scala.Scala
import com.mwz.sonar.scala.util.JavaOptionals._
import org.sonar.api.batch.fs.{FileSystem, InputComponent, InputFile}
import org.sonar.api.batch.sensor.{Sensor, SensorContext, SensorDescriptor}
import org.sonar.api.config.Configuration;
import org.sonar.api.utils.log.Loggers
import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}

/**
 *  Main sensor for importing Scoverage reports to SonarQube.
 *
 *  @author BalmungSan
 */
final class ScoverageSensor extends Sensor {
  private[this] val logger = Loggers.get(classOf[ScoverageSensor])

  /** Populates the [[SensorDescriptor]] of this sensor. */
  override def describe(descriptor: SensorDescriptor): Unit = {
    descriptor
      .onlyOnLanguage(Scala.Key)
      .onlyOnFileType(InputFile.Type.MAIN)
      .name(ScoverageSensor.SensorName)
  }

  /** Saves in SonarQube the scoverage information of a module */
  override def execute(context: SensorContext): Unit = {
    logger.info("[scoverage] Initializing the scoverage sensor")
    val reportFilename = getScoverageReportFilename(context.config())
    Try(ScoverageReportParser.parse(reportFilename)) match {
      case Success(moduleCoverage) => {
        logger.info(s"[scoverage] Successfully loaded the scoverage report file: '${reportFilename}'")

        logger.debug("[scoverage] Saving the overall scoverage information of the module")
        saveComponentScoverage(context, context.module(), moduleCoverage.moduleScoverage)

        //save the coverage information of each module file
        for (file <- getModuleSourceFiles(context.fileSystem())) {
          val filename = file.filename()
          logger.debug(s"[scoverage] Saving the scoverage information of the file: '${filename}'")
          moduleCoverage.files.get(filename) match {
            case Some(fileCoverage) => {
              //save the file overall scoverage information
              saveComponentScoverage(context, file, fileCoverage.fileScoverage)

              //save the coverage of each file line
              val coverage = context.newCoverage()
              coverage.onFile(file)
              for ((linenum, hits) <- fileCoverage.lines) coverage.lineHits(linenum, hits)
              coverage.save()
            }
            case None => {
              logger.warn(
                s"[scoverage] The file: '${filename}', has no scoverage information associated to it."
              )
            }
          }
        }
      }
      case Failure(ex) => {
        logger.error(
          s"""[scoverage] Aborting the scoverage sensor execution,
             |cause: an error occurred while reading the scoverage report file: '${reportFilename}',
             |the error was: ${ex.getMessage}.""".stripMargin
        )
      }
    }
  }

  /** Returns all scala main files from this module */
  private[this] def getModuleSourceFiles(fs: FileSystem): Iterable[InputFile] = {
    val predicates = fs.predicates
    val predicate = predicates.and(predicates.hasLanguage(Scala.Key), predicates.hasType(InputFile.Type.MAIN))
    fs.inputFiles(predicate).asScala
  }

  /** Returns the filename of the scoverage report for this module */
  private[this] def getScoverageReportFilename(settings: Configuration): String = {
    settings
      .get(ScoverageSensor.ScoverageReportPathPropertyKey)
      .toOption
      .getOrElse(
        ScoverageSensor.getDefaultScoverageReportPath(Scala.getScalaVersion(settings))
      )
  }

  /** Saves the [[ScoverageMetrics]] of a component */
  private[this] def saveComponentScoverage(
    context: SensorContext,
    component: InputComponent,
    scoverage: Scoverage
  ): Unit = {
    context
      .newMeasure[java.lang.Integer]()
      .on(component)
      .forMetric(ScoverageMetrics.totalStatements)
      .withValue(scoverage.totalStatements)
      .save()

    context
      .newMeasure[java.lang.Integer]()
      .on(component)
      .forMetric(ScoverageMetrics.coveredStatements)
      .withValue(scoverage.coveredStatements)
      .save()

    context
      .newMeasure[java.lang.Double]()
      .on(component)
      .forMetric(ScoverageMetrics.statementCoverage)
      .withValue(scoverage.statementCoverage)
      .save()

    context
      .newMeasure[java.lang.Double]()
      .on(component)
      .forMetric(ScoverageMetrics.branchCoverage)
      .withValue(scoverage.branchCoverage)
      .save()
  }
}

object ScoverageSensor {
  private val SensorName = "Scoverage Sensor"
  private val ScoverageReportPathPropertyKey = "sonar.scala.scoverage.reportPath"
  private def getDefaultScoverageReportPath(scalaVersion: String) =
    s"target/scala-${scalaVersion}/scoverage-report/scoverage.xml"
}