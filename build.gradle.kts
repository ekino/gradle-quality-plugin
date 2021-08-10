/*
 * Copyright (c) 2020 ekino (https://www.ekino.com/)
 */

import se.bjurr.gitchangelog.plugin.gradle.GitChangelogTask

plugins {
  `java-gradle-plugin`
  `kotlin-dsl`
  jacoco
  id("net.researchgate.release") version "2.8.1"
  id("se.bjurr.gitchangelog.git-changelog-gradle-plugin") version "1.71.4"
  id("org.sonarqube") version "3.3"
  id("com.gradle.plugin-publish") version "0.15.0"
}

repositories {
  jcenter()
}

dependencies {
  implementation("org.sonarsource.scanner.gradle:sonarqube-gradle-plugin:${property("sonarqubePluginVersion")}")

  testImplementation(gradleTestKit())
  testImplementation("org.junit.jupiter:junit-jupiter:${property("junitVersion")}")
  testImplementation("io.strikt:strikt-jvm:${property("striktVersion")}")
  testImplementation("io.mockk:mockk:${property("mockkVersion")}")
}

tasks.test {
  // To use JUnit5 Jupiter
  useJUnitPlatform()

  // Tests summary (displayed at the end)
  // waiting for https://github.com/gradle/gradle/issues/5431 in order to have a better way to do that
  addTestListener(object : TestListener {
    override fun beforeSuite(suite: TestDescriptor) {}
    override fun beforeTest(testDescriptor: TestDescriptor) {}
    override fun afterTest(testDescriptor: TestDescriptor, result: TestResult) {}
    override fun afterSuite(suite: TestDescriptor, result: TestResult) {
      if (suite.parent == null) {
        println("\nTests result: ${result.resultType}")
        println("Tests summary: ${result.testCount} tests, " +
                "${result.successfulTestCount} succeeded, " +
                "${result.failedTestCount} failed, " +
                "${result.skippedTestCount} skipped")
      }
    }
  })
}

val pluginName = "gradleQuality"
gradlePlugin {
  plugins {
    register(pluginName) {
      id = "com.ekino.oss.gradle.plugin.quality"
      implementationClass = "com.ekino.oss.gradle.plugin.quality.QualityPlugin"
    }
  }
}

pluginBundle {
  website = "https://github.com/ekino/gradle-quality-plugin"
  vcsUrl = "https://github.com/ekino/gradle-quality-plugin"
  description = "Quality plugin applying some configuration for your builds (checkstyle, jacoco, sonarqube)"

  (plugins) {
    named(pluginName) {
      displayName = "Java plugin"
      tags = listOf("ekino", "checkstyle", "jacoco", "sonarqube")
      version = version
    }
  }
}

val gitChangelogTask by tasks.registering(GitChangelogTask::class) {
  file = File("CHANGELOG.md")
  templateContent = file("changelog.mustache").readText()
}

tasks.jacocoTestReport {
  reports {
    xml.isEnabled = true
  }
}

sonarqube {
  properties {
    property("sonar.projectKey", "ekino_gradle-quality-plugin")
    property("sonar.java.coveragePlugin", "jacoco")
    property("sonar.coverage.jacoco.xmlReportPaths", "${buildDir}/reports/jacoco/test/jacocoTestReport.xml")
  }
}
