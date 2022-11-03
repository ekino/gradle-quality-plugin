/*
 * Copyright (c) 2020 ekino (https://www.ekino.com/)
 */

plugins {
  `java-gradle-plugin`
  `kotlin-dsl`
  jacoco
  id("net.researchgate.release") version "3.0.2"
  id("org.sonarqube") version "3.5.0.2730"
  id("com.gradle.plugin-publish") version "1.0.0"
  id( "pl.droidsonroids.jacoco.testkit") version "1.0.9"
}

repositories {
  mavenCentral()
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

gradlePlugin {
  plugins {
    create("gradleQuality") {
      id = "com.ekino.oss.gradle.plugin.quality"
      implementationClass = "com.ekino.oss.gradle.plugin.quality.QualityPlugin"
      displayName = "Gradle Java quality plugin"
    }
  }
}

pluginBundle {
  website = "https://github.com/ekino/gradle-quality-plugin"
  vcsUrl = "https://github.com/ekino/gradle-quality-plugin"
  description = "Quality plugin applying some configuration for your builds (checkstyle, jacoco, sonarqube)"
  tags = listOf("ekino", "checkstyle", "jacoco", "sonarqube")
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
