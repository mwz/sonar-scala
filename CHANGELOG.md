Changelog
===

## [7.4.0](https://github.com/mwz/sonar-scala/releases/tag/v7.4.0) - 11.03.2019
- Added support for [SonarQube 7.6](https://www.sonarqube.org/sonarqube-7-6). (#152 - @BalmungSan)
- Added an optional `sonar.scala.scoverage.disable` property, which can be used to disable the Scoverage sensor (defaults to `false`). (#169 - @BalmungSan)

## [6.8.0 LTS](https://github.com/mwz/sonar-scala/releases/tag/v6.8.0) - 03.03.2019
- Backported features and bug fixes from the 7.x series - new unit test metrics such as unit test count, duration of each test and the number of skipped and failed tests - see the release notes from [7.3.0](https://github.com/mwz/sonar-scala/releases/tag/v7.3.0) and [7.3.1](https://github.com/mwz/sonar-scala/releases/tag/v7.3.1) releases for more details. (#164 - @mwz)

## [7.3.1](https://github.com/mwz/sonar-scala/releases/tag/v7.3.1) - 27.01.2019
- Fixed Scalastyle rule lookup for custom rules created from templates. (#148 - @satabin)

## [7.3.0](https://github.com/mwz/sonar-scala/releases/tag/v7.3.0) - 30.12.2018
- Implemented a new sensor which saves unit test metrics such as unit test count, duration of each test and the number of skipped and failed tests. To use the new sensor, set the `sonar.tests` property which should point to directories containing tests (usually `src/test/scala`) and make sure you run your unit tests before you trigger sonar-scanner analysis. That's all you need to do in sbt unless you've changed the default location where sbt saves JUnit XML reports - then you'll also need to set the `sonar.junit.reportPaths` property (which defaults to `target/test-reports`). For projects using Gradle, setting both properties is necessary to make use of the sensor (Gradle outputs those in `build/test-results/test`), and Maven additionally requires the Surefire plugin to be installed. Please refer to the [sample](examples/) projects for example configuration for each build tool. (#143 - @mwz)
- Fixed incorrect log messages about missing `sonar.scala.version` property. (#139 - @mwz)

## [6.7.0 LTS](https://github.com/mwz/sonar-scala/releases/tag/v6.7.0) - 09.12.2018
- Backported features from the 7.x series - new `Recommended by sonar-scala` quality profile and cleaned up Scapegoat rule repository, see the notes from [7.2.0](https://github.com/mwz/sonar-scala/releases/tag/v7.2.0) release for more details. (#136 - @mwz)

## [7.2.0](https://github.com/mwz/sonar-scala/releases/tag/v7.2.0) - 03.12.2018
- Added a new quality profile `Recommended by sonar-scala`, which is a combination of Scalastyle and Scapegoat rules. We recommend using this profile as it excludes any duplicate rules and contains custom instances of Scalastyle templates set up in accordance with the current style guides recommended by the community. (#130 - @mwz)
- Cleaned up Scapegoat rule repository and quality profiles by removing any Scapegoat rules which are no longer active in the upstream project.  (#132 - @mwz)

## [7.1.0](https://github.com/mwz/sonar-scala/releases/tag/v7.1.0) - 19.11.2018
- Added support for [SonarQube 7.4](https://www.sonarqube.org/sonarqube-7-4). (#127 - @mwz)

## [7.0.0](https://github.com/mwz/sonar-scala/releases/tag/v7.0.0) - 08.10.2018
- Added support for [SonarQube 7.3](https://www.sonarqube.org/sonarqube-7-3). The current `6.x` series targeting *SonarQube 6.7 LTS* will be still maintained and will follow the SonarQube LTS lifecycle. (#120 - @mwz)
- Removed the following:
  - `sonar.scoverage.reportPath` property which was deprecated in [6.2.0](https://github.com/mwz/sonar-scala/releases/tag/v6.2.0) - please use `sonar.scala.scoverage.reportPath` instead;
  - the old Scalastyle quality profile and rule registry which were deprecated in [6.6.0](https://github.com/mwz/sonar-scala/releases/tag/v6.6.0); (#120 - @mwz)
- Bumped up the default value of the `sonar.scala.version` property to `2.12`. (#120 - @mwz)

## [6.6.0 LTS](https://github.com/mwz/sonar-scala/releases/tag/v6.6.0) - 30.09.2018
- Refactored Scalastyle module and introduced new Scalastyle rules repository along with a new quality profile, which now in addition to the latest Scalastyle rules also consists of rule templates which are activated by default. The existing Scalastyle quality profile and rule registry have been deprecated and will be removed in the next major version of the plugin (7.x). The new module also exposes the following optional property `sonar.scala.scalastyle.disable`, which allows you to disable the Scalastyle sensor. (#35 - @mwz)
- New `Scalastyle+Scapegoat` quality profile, which includes all of the default rules from Scalastyle and Scapegoat quality profiles. (#112 - @BalmungSan)
- Bumped Scapegoat up to `1.3.7`, which introduces a new inspection ([UnsafeStringContains](https://github.com/sksamuel/scapegoat/blob/v1.3.7/src/main/scala/com/sksamuel/scapegoat/inspections/string/UnsafeStringContains.scala)) and other various fixes and improvements - see the [diff](https://github.com/sksamuel/scapegoat/compare/v1.3.5...v1.3.7) for more details. (#100 - @mwz)
- Scapegoat sensor now also works with absolute file paths in the Scapegoat report. (#116 - @mwz)

## [6.5.1 LTS](https://github.com/mwz/sonar-scala/releases/tag/v6.5.1) - 11.08.2018
- This release fixes an issue with Scapegoat sensor using a single report for each module in multi-module projects. (#96 - @mwz)

## [6.5.0 LTS](https://github.com/mwz/sonar-scala/releases/tag/v6.5.0) - 09.07.2018
- Introduced support for [Scapegoat](https://github.com/sksamuel/scapegoat) linter. This version of the plugin ships with a **Scapegoat** rule repository, quality profile as well as a new sensor, which reads the xml report generated by Scapegoat and submits Scapegoat issues to SonarQube. To use this feature, you need to set up Scapegoat in your project and make sure you generate a Scapegoat report before running `sonar-scanner`, see [sbt-scapegoat](https://github.com/sksamuel/sbt-scapegoat) for SBT integration. This feature exposes the following optional properties:
  - `sonar.scala.scapegoat.reportPath` - relative path to the scapegoat report (defaults to `target/scala-${sonar.scala.version}/scapegoat-report/scapegoat.xml`) and
  - `sonar.scala.scapegoat.disable` - which allows you to disable the Scapegoat sensor from being executed on your sources (defaults to `false`)

  A massive thanks to @BalmungSan for implementing this feature! (#8 - @BalmungSan, @mwz)

## [6.4.0 LTS](https://github.com/mwz/sonar-scala/releases/tag/v6.4.0) - 23.05.2018
- Improved logging in the Scoverage sensor. (#61 - @BalmungSan)
- Fixed Scoverage issues with multi-module Gradle projects. Please see the [examples](https://github.com/mwz/sonar-scala/tree/master/examples) for a reference on how to configure Gradle projects and how to execute SonarQube analysis. (#63 - @mwz)

## [6.3.0 LTS](https://github.com/mwz/sonar-scala/releases/tag/v6.3.0) - 12.05.2018
- Use semantic versioning scheme to parse the `sonar.scala.version` property. The patch version is now ignored and `2.11` and `2.12` are valid version numbers. In case of a missing value or an incorrect version the property defaults to `2.11.0`. (#53 - @BalmungSan)
- Fixed an issue which affected Gradle users and caused the sonar sources prefix `sonar.sources` to be appended twice to filenames in the Scoverage report. (#56 - @mwz)

## [6.2.0 LTS](https://github.com/mwz/sonar-scala/releases/tag/v6.2.0) - 28.04.2018
- Added support for multiple source locations configured by the `sonar.sources` property. As per SonarQube [documentation](https://docs.sonarqube.org/display/SONAR/Analysis+Parameters), the paths should be separated by a comma. Additionally, the paths can now be absolute, which allows sonar-scala to work with [SonarQube Maven plugin](https://docs.sonarqube.org/display/SCAN/Analyzing+with+SonarQube+Scanner+for+Maven). (#52 - @mwz)
- The Scoverage report path property `sonar.scoverage.reportPath` was deprecated and will be removed in the next major version. Please use `sonar.scala.scoverage.reportPath` instead. (#46 - @BalmungSan)

## [6.1.0 LTS](https://github.com/mwz/sonar-scala/releases/tag/v6.1.0) - 28.03.2018
- The Scoverage sensor was rewritten from scratch; introduced a new branch coverage metric. (#34 - @BalmungSan)
- Addressed coverage measure warnings reported by sonar-scanner during analysis. (#18 - @BalmungSan)
- Added a new property `sonar.scala.version` to specify Scala version. (#2 - @ElfoLiNk)

## [6.0.0 LTS](https://github.com/mwz/sonar-scala/releases/tag/v6.0.0) - 10.02.2018
- Support for SonarQube 6.7.1 LTS. (#5 - @mwz)
