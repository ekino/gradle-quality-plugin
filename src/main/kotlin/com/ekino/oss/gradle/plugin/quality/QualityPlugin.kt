/*
 * Copyright (c) 2019 ekino (https://www.ekino.com/)
 */

package com.ekino.oss.gradle.plugin.quality

import com.ekino.oss.gradle.plugin.quality.task.PrintCoverageTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.quality.CheckstyleExtension
import org.gradle.api.plugins.quality.CheckstylePlugin
import org.gradle.api.resources.MissingResourceException
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.*
import org.gradle.testing.jacoco.plugins.JacocoPlugin
import org.gradle.testing.jacoco.tasks.JacocoReport
import org.sonarqube.gradle.SonarQubeExtension
import org.sonarqube.gradle.SonarQubePlugin

class QualityPlugin: Plugin<Project> {

  override fun apply(project: Project) {
    with(project) {

      // plugins
      apply<JavaPlugin>()
      apply<CheckstylePlugin>()
      apply<JacocoPlugin>()
      apply<SonarQubePlugin>()

      val sourceSets = the<SourceSetContainer>()

      // configuration
      configure(listOf(project)) {

        // Checkstyle configuration
        configure<CheckstyleExtension> {
          toolVersion = "8.24"
          config = resources.text.fromString(getCheckstyleConfig())
        }

        // Jacoco configuration
        val jacocoTestReport by tasks.named<JacocoReport>("jacocoTestReport") {
          doFirst {
            println("Generating jacoco coverage report in HTML ...")
          }
          // To add ".exec" file from integrationTest task if exists
          tasks.findByName("integrationTest")?.let {
            executionData.from(file("${buildDir}/jacoco/integrationTest.exec")) // To merge Test Tasks Jacoco reports
          }
        }

        val printCoverage by tasks.register<PrintCoverageTask>("printCoverage") {
          htmlJacocoReport = "$buildDir/reports/jacoco/test/html/index.html"
          mustRunAfter(jacocoTestReport)
        }

        tasks.named("build") {
          dependsOn(jacocoTestReport, printCoverage)
        }

        if (project.hasProperty("sonarCoverageExclusions")) {
          val excludes = (findProperty("sonarCoverageExclusions") as String).replace(".java", ".class").split(",\\s*".toRegex())
          afterEvaluate {
            jacocoTestReport.classDirectories.setFrom(sourceSets["main"].output.classesDirs.asFileTree.matching {
              exclude(excludes)
            })
          }
        }

        // Sonar configuration
        configure<SonarQubeExtension> {
          properties {
            project.findProperty("SONARQUBE_URL")?.let { property("sonar.host.url", it) }
            project.findProperty("SONARQUBE_TOKEN")?.let { property("sonar.login", it) }
            project.findProperty("sonarCoverageExclusions")?.let { property("sonar.coverage.exclusions", it) }
            project.findProperty("sonarExclusions")?.let { property("sonar.exclusions", it) }
            tasks.findByName("integrationTest")?.let {
              properties.remove("sonar.jacoco.reportPath")
              property("sonar.jacoco.reportPaths", "${buildDir}/jacoco/test.exec, ${buildDir}/jacoco/integrationTest.exec")
              property("sonar.junit.reportPaths", "${buildDir}/test-results/all")
              property("sonar.tests", sourceSets["test"].allJava.srcDirs.filter { it.exists() } + sourceSets["integrationTest"].allJava.srcDirs.filter { it.exists() })
            }
          }
        }

        afterEvaluate {
          tasks.named("sonarqube") {
            tasks.findByName("integrationTest")?.let {
              tasks.findByName("aggregateJunitReports")?.let {
                dependsOn("aggregateJunitReports")
              }
            }
          }
        }
      }
    }
  }

  internal fun getCheckstyleConfig() = getFilePath()?.use { String(it.readAllBytes()) }
          ?: throw MissingResourceException("The checkstyle config file cannot be found")

  internal fun getFilePath() = QualityPlugin::class.java.classLoader?.getResourceAsStream("checkstyle.xml")

}
