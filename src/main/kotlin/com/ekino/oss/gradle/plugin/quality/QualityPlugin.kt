/*
 * Copyright (c) 2020 ekino (https://www.ekino.com/)
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
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.the
import org.gradle.kotlin.dsl.withType
import org.gradle.testing.jacoco.plugins.JacocoPlugin
import org.gradle.testing.jacoco.plugins.JacocoReportAggregationPlugin
import org.gradle.testing.jacoco.tasks.JacocoReport
import org.sonarqube.gradle.SonarExtension
import org.sonarqube.gradle.SonarQubePlugin

class QualityPlugin: Plugin<Project> {

  override fun apply(project: Project) {
    with(project) {

      // plugins
      apply<JavaPlugin>()
      apply<CheckstylePlugin>()
      apply<JacocoPlugin>()
      apply<SonarQubePlugin>()
      apply<JacocoReportAggregationPlugin>()

      val sourceSets = the<SourceSetContainer>()

      // configuration
      configure(listOf(project)) {

        // Checkstyle configuration
        configure<CheckstyleExtension> {
          config = resources.text.fromString(getCheckstyleConfig())
          toolVersion = "9.3"
        }

        // Jacoco configuration
        val jacocoTestReports = tasks.withType<JacocoReport> {
          doFirst {
            println("Generating jacoco coverage report in HTML ...")
          }
          reports {
            xml.required.set(true)
            csv.required.set(false)
            html.required.set(true)
          }
        }

        val printCoverage by tasks.register<PrintCoverageTask>("printCoverage") {
          htmlJacocoReport = "$buildDir/reports/jacoco/test/html/index.html"
          mustRunAfter(jacocoTestReports)
        }

        tasks.named("build") {
          dependsOn(jacocoTestReports, printCoverage)
        }

        if (project.hasProperty("sonarCoverageExclusions")) {
          val excludes = (findProperty("sonarCoverageExclusions") as String).replace(".java", ".class").split(",\\s*".toRegex())
          afterEvaluate {
            jacocoTestReports.configureEach {
              classDirectories.setFrom(sourceSets["main"].output.classesDirs.asFileTree.matching {
                exclude(excludes)
              })
            }
          }
        }

        // Sonar configuration
        configure<SonarExtension> {
          properties {
            project.findProperty("SONARQUBE_URL")?.let { property("sonar.host.url", it) }
            project.findProperty("SONARQUBE_TOKEN")?.let { property("sonar.login", it) }
            project.findProperty("sonarCoverageExclusions")?.let { property("sonar.coverage.exclusions", it) }
            project.findProperty("sonarExclusions")?.let { property("sonar.exclusions", it) }
            property("sonar.coverage.jacoco.xmlReportPaths", "${buildDir}/reports/jacoco/test/*.xml")
          }
        }
      }
    }
  }

  internal fun getCheckstyleConfig() = getFilePath()?.use { String(it.readBytes()) }
          ?: throw MissingResourceException("The checkstyle config file cannot be found")

  internal fun getFilePath() = QualityPlugin::class.java.classLoader?.getResourceAsStream("checkstyle.xml")

}
