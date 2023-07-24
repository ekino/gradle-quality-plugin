/*
 * Copyright (c) 2023 ekino (https://www.ekino.com/)
 */

plugins {
  `java-gradle-plugin`
  `kotlin-dsl`
  jacoco
  id("net.researchgate.release") version "3.0.2"
  id("com.gradle.plugin-publish") version "1.2.0"
}

repositories {
  mavenCentral()
  maven { setUrl("https://plugins.gradle.org/m2/") }
}

dependencies {
  implementation("org.sonarsource.scanner.gradle:sonarqube-gradle-plugin:4.0.0.2929")

  testImplementation(gradleTestKit())
  testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
  testImplementation("io.strikt:strikt-jvm:0.34.1")
  testImplementation("io.mockk:mockk:1.13.5")
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
  website.set("https://github.com/ekino/gradle-quality-plugin")
  vcsUrl.set("https://github.com/ekino/gradle-quality-plugin")
  plugins {
    create("gradleQuality") {
      id = "com.ekino.oss.gradle.plugin.quality"
      implementationClass = "com.ekino.oss.gradle.plugin.quality.QualityPlugin"
      displayName = "Gradle Java quality plugin"
      description = "Quality plugin applying some configuration for your builds (checkstyle, jacoco, sonarqube)"
      tags.set(listOf("ekino", "checkstyle", "jacoco", "sonarqube"))
    }
  }
}

tasks.jacocoTestReport {
  reports {
    xml.required.set(true)
  }
}
