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

import org.scalastyle._

final case class ScalastyleInspection(
  clazz: String,
  id: String,
  label: String,
  description: String,
  extraDescription: Option[String],
  justification: Option[String],
  defaultLevel: Level,
  params: Seq[Param]
)

final case class Param(
  name: String,
  typ: ParameterType,
  label: String,
  description: String,
  default: String
)

object ScalastyleInspections {
  val AllInspections: List[ScalastyleInspection] = ???
  val AllInspectionsByClass: Map[String, ScalastyleInspection] =
    AllInspections.map(i => i.clazz -> i).toMap
}
