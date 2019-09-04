/*
 * Copyright (c) 2019 ekino (https://www.ekino.com/)
 */

package com.ekino.oss.gradle.plugin.quality

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.quality.Checkstyle
import org.gradle.api.plugins.quality.CheckstyleExtension
import org.gradle.api.plugins.quality.CheckstylePlugin
import org.gradle.api.resources.TextResource
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.*
import org.gradle.testing.jacoco.plugins.JacocoPlugin
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension
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
          toolVersion = "8.19"
        }

        tasks.withType(Checkstyle::class.java) {
          config = getTextResource(project, "checkstyle.xml")
        }

        // Jacoco configuration
        configure<JacocoPluginExtension> {
          toolVersion = "0.8.3"
        }

        val jacocoTestReport by tasks.named<JacocoReport>("jacocoTestReport") {
          doFirst {
            println("Generating jacoco coverage report in HTML ...")
          }
          doFirst {
            // To load only ".exec" files that are really present
            executionData.setFrom(files(executionData.files.filter {
              it.exists()
            }))
          }
        }

        tasks.named("build") {
          dependsOn(jacocoTestReport)
        }

        tasks.findByName("integrationTest")?.let {
          val test by tasks.named("test")
          val integrationTest by tasks.named("integrationTest")
          jacocoTestReport.executionData.from(test, integrationTest) // To merge Test Tasks Jacoco reports
        }

        if (project.hasProperty("sonarCoverageExclusions")) {
          val excludes = (findProperty("sonarCoverageExclusions") as String).split("/,\\s*/")
          //jacocoTestReport.classDirectories.setFrom(files(sourceSets["main"].output.classesDirs.filter {
          //    fileTree("dir" to it, "exclude" to excludes)
          //})
        }

        // Sonar configuration
        configure<SonarQubeExtension> {
          properties {
            property("sonar.host.url", project.findProperty("SONARQUBE_URL") ?: "")
            property("sonar.login", project.findProperty("SONARQUBE_TOKEN") ?: "")
            property("sonar.coverage.exclusions", project.findProperty("sonarCoverageExclusions") ?: "")
            property("sonar.exclusions", project.findProperty("sonarExclusions") ?: "")
          }
        }

        tasks.findByName("integrationTest")?.let {
          tasks.findByName("aggregateJunitReports").let {
            val sonarqube by tasks.named("sonarqube")
            sonarqube.dependsOn("aggregateJunitReports")
          }

          configure<SonarQubeExtension> {
            properties {
              property("sonar.jacoco.reportPath", "")
              property("sonar.jacoco.reportPaths", "${buildDir}/jacoco/integrationTest.exec, ${buildDir}/jacoco/test.exec")
              property("sonar.junit.reportPaths", "${buildDir}/test-results/all")
              properties["sonar.tests"] = sourceSets["test"].allJava.srcDirs.filter { it.exists() } + sourceSets["integrationTest"].allJava.srcDirs.filter { it.exists() }
            }
          }
        }
      }
    }
  }

  private fun getTextResource(project: Project, file: String): TextResource {
    return project.resources.text.fromString(QualityPlugin::class.java.getClassLoader().getResource(file).readText())
  }

}
