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
import java.nio.file.{Path, Paths}

import com.sksamuel.scapegoat.{Inspection, ScapegoatConfig}
import sbt.Keys._
import sbt._

import scala.meta._

/** SBT Task that generates a managed file with all scapegoat inspections */
object ScapegoatInspectionsGenerator {

  /** Scapegoat inspections that won't be included in the generated file */
  val BlacklistedInspections: Set[String] = Set.empty

  val generatorTask = Def.task {
    val log = streams.value.log
    log.info("Generating Scapegoat inspections file.")

    // Load the template file.
    val templateFile: Path = Paths
      .get(
        baseDirectory.value.toString,
        "project",
        "src",
        "main",
        "resources",
        "ScapegoatInspections.scala"
      )

    val allScapegoatInspections: List[(String, Inspection)] = extractInspections()
    val stringifiedScapegoatIsnpections: List[String] = stringifyInspections(allScapegoatInspections)
    val transformed: Tree = fillTemplate(templateFile.parse[Source].get, stringifiedScapegoatIsnpections)

    val scapegoatInspectionsFile: File = (sourceManaged in Compile).value / "scapegoat" / "inspections.scala"
    IO.write(scapegoatInspectionsFile, transformed.syntax)

    Seq(scapegoatInspectionsFile)
  }

  /**
   * Returns all scapegoat inspections, except the ones that should be ignored
   */
  def extractInspections(): List[(String, Inspection)] =
    ScapegoatConfig.inspections.collect {
      case inspection if !BlacklistedInspections.contains(inspection.getClass.getName) =>
        (inspection.getClass.getName, inspection)
    }.toList

  /** Stringifies a list of scapegoat inspections */
  def stringifyInspections(scapegoatInspections: List[(String, Inspection)]): List[String] =
    scapegoatInspections map {
      case (inspectionClassName, inspection) =>
        s"""ScapegoatInspection(
           |  id = "$inspectionClassName",
           |  name = "${inspection.text}",
           |  description = ${inspection.explanation.map(text => s""""$text"""")},
           |  defaultLevel = Level.${inspection.defaultLevel}
           |)""".stripMargin
    }

  /** Fill the template file */
  def fillTemplate(template: Source, stringified: List[String]): Tree = {
    val term: Term = stringified.toString.parse[Term].get
    template.transform {
      case q"val AllInspections: $tpe = $expr" =>
        q"val AllInspections: $tpe = $term"
    }
  }
}
