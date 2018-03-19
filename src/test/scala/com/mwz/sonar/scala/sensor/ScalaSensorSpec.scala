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
package com.mwz.sonar.scala.sensor

import java.nio.file.Paths

import org.scalatest._
import org.sonar.api.batch.fs.internal.TestInputFileBuilder
import org.sonar.api.batch.sensor.internal.{DefaultSensorDescriptor, SensorContextTester}
import org.sonar.api.batch.sensor.measure.Measure
import org.sonar.api.config.internal.MapSettings
import org.sonar.api.measures.{CoreMetrics => CM}

/**
 *  Tests the Scala Sensor
 *
 *  @author mwz
 */
class ScalaSensorSpec extends FlatSpec with Matchers with OptionValues {
  val sensor = new ScalaSensor()
  behavior of "A ScalaSensor"

  it should "correctly set descriptor" in {
    val descriptor = new DefaultSensorDescriptor
    sensor.describe(descriptor)

    descriptor.name() shouldBe "Scala Sensor"
    descriptor.languages() should have size 1
    descriptor.languages().iterator().next() shouldBe "scala"
  }

  it should "correctly measure ScalaFile1" in {
    val context = SensorContextTester.create(Paths.get("./src/test/resources"))
    val inputFile =
      TestInputFileBuilder.create("", "src/test/resources/ScalaFile1.scala").setLanguage("scala").build()
    context.fileSystem().add(inputFile)
    sensor.execute(context)

    val componentKey = inputFile.moduleKey() + inputFile.key()

    checkMetric(context, componentKey, CM.FILES_KEY, 1)
    checkMetric(context, componentKey, CM.COMMENT_LINES_KEY, 0)
    checkMetric(context, componentKey, CM.CLASSES_KEY, 1)
    checkMetric(context, componentKey, CM.FUNCTIONS_KEY, 1)
    checkMetric(context, componentKey, CM.NCLOC_KEY, 6)
  }

  it should "correctly measure ScalaFile2" in {
    val context = SensorContextTester.create(Paths.get("./src/test/resources"))
    val inputFile =
      TestInputFileBuilder.create("", "src/test/resources/ScalaFile2.scala").setLanguage("scala").build()
    context.fileSystem().add(inputFile)
    sensor.execute(context)

    val componentKey = inputFile.moduleKey() + inputFile.key()

    checkMetric(context, componentKey, CM.FILES_KEY, 1)
    checkMetric(context, componentKey, CM.COMMENT_LINES_KEY, 1)
    checkMetric(context, componentKey, CM.CLASSES_KEY, 2)
    checkMetric(context, componentKey, CM.FUNCTIONS_KEY, 2)
  }

  private def checkMetric(
    sensorContext: SensorContextTester,
    componentKey: String,
    metricKey: String,
    value: Int
  ): Unit = {
    val measure: Option[Measure[Integer]] = Option(sensorContext.measure(componentKey, metricKey))
    measure shouldBe defined
    measure.value.value() shouldBe int2Integer(value)
  }
}