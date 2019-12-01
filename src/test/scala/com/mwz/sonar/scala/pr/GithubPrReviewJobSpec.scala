/*
 * Copyright (C) 2018-2019  All sonar-scala contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mwz.sonar.scala.pr

import com.mwz.sonar.scala.GlobalConfig
import com.mwz.sonar.scala.pr.Generators._
import com.mwz.sonar.scala.pr.github.{Comment, NewComment, NewStatus, User}
import org.http4s.Uri
import org.scalacheck.ScalacheckShapeless._
import org.scalatest.{FlatSpec, Matchers}
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import org.sonar.api.batch.fs.InputFile
import org.sonar.api.batch.postjob.internal.DefaultPostJobDescriptor
import org.sonar.api.batch.rule.Severity
import org.sonar.api.config.internal.MapSettings
import org.sonar.api.rule.RuleKey

class GithubPrReviewJobSpec extends FlatSpec with Matchers with ScalaCheckDrivenPropertyChecks {
  it should "correctly set the descriptor" in {
    val conf = new GlobalConfig(new MapSettings().asConfig())
    val job = new GithubPrReviewJob(conf, new GlobalIssues)
    val descriptor = new DefaultPostJobDescriptor
    job.describe(descriptor)

    descriptor.name shouldBe "Github PR review job"
  }

  it should "lookup existing comments for issues" in {
    forAll { (issue: Issue, otherIssue: Issue) =>
      val issues = Map(
        issue.file -> List(issue),
        otherIssue.file -> List(otherIssue)
      )
      val patchLineMapping = Map(
        issue.file.toString -> Right(Map(FileLine(issue.line) -> PatchLine(7)))
      )
      val comment = Comment(1, issue.file.toString, Some(7), User("usr"), "comment")
      val comments = Map(
        issue.file.toString -> List(
          comment,
          Comment(2, issue.file.toString, Some(123), User("usr"), "comment"),
          Comment(3, "otherFile", Some(123), User("usr"), "comment")
        )
      )

      val expected = Map(
        issue.file -> Map(PatchLine(7) -> List((issue, List(comment))))
      )

      GithubPrReviewJob.allCommentsForIssues(issues, patchLineMapping, comments) shouldBe expected
    }
  }

  it should "not return the lookup for no comments" in {
    forAll { (issue: Issue, otherIssue: Issue) =>
      val issues = Map(
        issue.file -> List(issue),
        otherIssue.file -> List(otherIssue)
      )
      val patchLineMapping = Map(
        issue.file.toString -> Right(Map(FileLine(issue.line) -> PatchLine(7)))
      )
      val comments = Map.empty[String, List[Comment]]

      GithubPrReviewJob.allCommentsForIssues(issues, patchLineMapping, comments) shouldBe Map.empty
    }
  }

  it should "create comments for new issues" in {
    forAll { (commit: String, patchLine: PatchLine, issuesComments: List[(Issue, List[Comment])]) =>
      val uri = Uri.unsafeFromString("https://hello.com")
      val issues =
        issuesComments
          .groupBy(v => (v._1.file))
          .mapValues(_.groupBy(v => PatchLine(v._1.line)))
      val newComments = issuesComments.map {
        case (issue, _) =>
          NewComment(Markdown.inline(uri, issue).text, commit, issue.file.toString, issue.line)
      }

      GithubPrReviewJob.commentsForNewIssues(uri, commit, issues) should
      contain theSameElementsAs newComments
    }
  }

  it should "ignore existing comments created by sonar-scala" in {
    forAll { (commit: String, ruleKey: RuleKey, inputFile: InputFile) =>
      val uri = Uri.unsafeFromString("https://hello.com")
      val issueToIgnore =
        Issue(ruleKey, inputFile, 2, Severity.MAJOR, "message 2")
      val commentToIgnore =
        Comment(2, "path", Some(2), User("user"), Markdown.inline(uri, issueToIgnore).text)
      val issues = List(
        (
          Issue(ruleKey, inputFile, 1, Severity.MINOR, "message 1"),
          List(Comment(1, "path", Some(1), User("user"), "body 1"))
        )
      )
      val allIssues = (issueToIgnore, commentToIgnore) :: issues

      val groupped =
        issues
          .groupBy(v => (v._1.file))
          .mapValues(_.groupBy(v => PatchLine(v._1.line)))

      val newComments = issues.map {
        case (issue, _) =>
          NewComment(Markdown.inline(uri, issue).text, commit, issue.file.toString, issue.line)
      }

      GithubPrReviewJob.commentsForNewIssues(uri, commit, groupped) should contain theSameElementsAs newComments
    }
  }

  it should "determine a review status based on found issues" in {
    forAll { (i: List[Issue]) =>
      val blockers = i.filter(_.severity === Severity.BLOCKER).size
      val critical = i.filter(_.severity === Severity.CRITICAL).size
      GithubPrReviewJob.reviewStatus(i.groupBy(_.file)) shouldBe ReviewStatus(blockers, critical)
    }
  }

  it should "infer status state from a pr status" in {
    GithubPrReviewJob.statusState(Pending) shouldBe "pending"
    GithubPrReviewJob.statusState(Success) shouldBe "success"
    GithubPrReviewJob.statusState(Error(ReviewStatus(0, 0))) shouldBe "error"
    GithubPrReviewJob.statusState(Failure(new Throwable)) shouldBe "failure"
  }

  it should "infer status description from a pr status" in {
    GithubPrReviewJob.statusDescription(Pending) shouldBe "SonarQube is reviewing this pull request."
    GithubPrReviewJob.statusDescription(Success) shouldBe "SonarQube didn't report any critical or blocker issues."
    GithubPrReviewJob.statusDescription(Error(ReviewStatus(1, 3))) shouldBe "SonarQube reported 1 blocker and 3 critical issues."
    GithubPrReviewJob.statusDescription(Failure(new Throwable)) shouldBe "An error occurred during SonarQube review."
  }

  it should "infer a new pr status from the result of the review" in {
    GithubPrReviewJob.githubStatus(Pending) shouldBe NewStatus(
      "pending",
      "",
      "SonarQube is reviewing this pull request.",
      "sonar-scala/review"
    )
  }
}
