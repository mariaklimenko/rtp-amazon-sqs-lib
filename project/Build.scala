import io.gatling.sbt.GatlingPlugin
import sbt.Keys._
import sbt._
import spray.revolver.RevolverPlugin._

object Build extends Build {
  val moduleName = "rtp-amazon-sqs-lib"

  val root = Project(id = moduleName, base = file(".")).enablePlugins(GatlingPlugin)
    .configs(IntegrationTest)
    .settings(Revolver.settings)
    .settings(Defaults.itSettings: _*)
    .settings(javaOptions in Test += "-Dconfig.resource=application.test.conf")
    .settings(run := (run in Runtime).evaluated) // Required to stop Gatling plugin overriding the default "run".
    .settings(
      name := moduleName,
      organization := "kissthinker",
      version := "1.0.0-SNAPSHOT",
      scalaVersion := "2.11.8",
      scalacOptions ++= Seq(
        "-feature",
        "-language:implicitConversions",
        "-language:higherKinds",
        "-language:existentials",
        "-language:reflectiveCalls",
        "-language:postfixOps",
        "-Yrangepos",
        "-Yrepl-sync"
      ),
      ivyScala := ivyScala.value map {
        _.copy(overrideScalaVersion = true)
      },
      resolvers ++= Seq(
        "Artifactory Snapshot Realm" at "http://artifactory.registered-traveller.homeoffice.gov.uk/artifactory/libs-snapshot-local/",
        "Artifactory Release Realm" at "http://artifactory.registered-traveller.homeoffice.gov.uk/artifactory/libs-release-local/",
        "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
        "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/",
        "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases",
        "Kamon Repository" at "http://repo.kamon.io"
      )
    )
    .settings(libraryDependencies ++= {
      val `akka-version` = "2.4.2"
      val `play-version` = "2.5.0"
      val `elasticmq-version` = "0.8.12"
      val `gatling-version` = "2.1.7"
      val `rtp-test-lib-version` = "1.2.1"
      val `rtp-io-lib-version` = "1.7.2"
      val `rtp-akka-lib-version` = "1.6.2"

      Seq(
        "com.typesafe.akka" %% "akka-actor" % `akka-version` withSources(),
        "com.typesafe.akka" %% "akka-remote" % `akka-version` withSources(),
        "com.typesafe.akka" %% "akka-stream" % `akka-version` withSources(),
        "com.typesafe.akka" %% "akka-slf4j"% `akka-version` withSources(),
        "com.typesafe.play" %% "play-ws" % `play-version` withSources(),
        "org.scalactic" %% "scalactic" % "2.2.6" withSources(),
        "ch.qos.logback" % "logback-classic" % "1.1.3" withSources(),
        "org.slf4j" % "jcl-over-slf4j" % "1.7.12" withSources(),
        "com.amazonaws" % "aws-java-sdk" % "1.10.62" exclude ("commons-logging", "commons-logging"),
        "de.flapdoodle.embed" % "de.flapdoodle.embed.mongo" % "1.50.2" withSources(),
        "uk.gov.homeoffice" %% "rtp-test-lib" % `rtp-test-lib-version` withSources(),
        "uk.gov.homeoffice" %% "rtp-io-lib" % `rtp-io-lib-version` withSources(),
        "uk.gov.homeoffice" %% "rtp-akka-lib" % `rtp-akka-lib-version` withSources()
      ) ++ Seq(
        "io.gatling.highcharts" % "gatling-charts-highcharts" % `gatling-version` % IntegrationTest withSources(),
        "io.gatling" % "gatling-test-framework" % `gatling-version` % IntegrationTest withSources(),
        "org.scalatest" %% "scalatest" % "2.2.4" % Test withSources(),
        "com.typesafe.akka" %% "akka-testkit" % `akka-version` % Test withSources(),
        "com.typesafe.play" %% "play-test" % `play-version` % Test withSources(),
        "com.typesafe.play" %% "play-specs2" % `play-version` % Test withSources(),
        "org.elasticmq" %% "elasticmq-core" % `elasticmq-version` % Test withSources(),
        "org.elasticmq" %% "elasticmq-rest-sqs" % `elasticmq-version` % Test withSources(),
        "uk.gov.homeoffice" %% "rtp-test-lib" % `rtp-test-lib-version` % Test classifier "tests" withSources(),
        "uk.gov.homeoffice" %% "rtp-io-lib" % `rtp-io-lib-version` % Test classifier "tests" withSources(),
        "uk.gov.homeoffice" %% "rtp-akka-lib" % `rtp-akka-lib-version` % Test classifier "tests" withSources() excludeAll ExclusionRule(organization = "org.specs2")
      )
    })
}