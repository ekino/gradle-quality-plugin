package com.ekino.frida.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.quality.Checkstyle

class QualityPlugin implements Plugin<Project> {

  @Override
  void apply(Project project) {

    // plugins
    project.plugins.apply('java')
    project.plugins.apply('checkstyle')
    project.plugins.apply('jacoco')
    project.plugins.apply('org.sonarqube')

    // configuration
    project.configure(project) {

      // Checkstyle configuration
      checkstyle {
        toolVersion = "8.19"
      }

      tasks.withType(Checkstyle) {
        config = getTextResource(project, "checkstyle.xml")
      }

      // Jacoco configuration
      build.dependsOn jacocoTestReport

      jacoco {
        toolVersion = "0.8.3"
      }

      jacocoTestReport {
        doFirst {
          println 'Generating jacoco coverage report in HTML ...'
        }
        doFirst { // To load only ".exec" files that are really present
          executionData.from = files(executionData.findAll {
            it.exists()
          })
        }
      }

      if (tasks.findByName('integrationTest')) {
        jacocoTestReport {
          executionData test, integrationTest // To merge Test Tasks Jacoco reports
        }
      }

      if (project.hasProperty("sonarCoverageExclusions")) {
        def excludes = project.property("sonarCoverageExclusions").split(/,\s*/) as List
        jacocoTestReport {
          afterEvaluate {
            classDirectories.from = files(sourceSets.main.output.classesDirs.collect {
              fileTree(dir: it, excludes: excludes)
            })
          }
        }
      }

      // Sonar configuration
      sonarqube {
        properties {
          property "sonar.host.url", project.hasProperty("SONARQUBE_URL") ? project.property("SONARQUBE_URL") : null
          property "sonar.login", project.hasProperty("SONARQUBE_TOKEN") ? project.property("SONARQUBE_TOKEN") : null
          property "sonar.coverage.exclusions", project.hasProperty("sonarCoverageExclusions") ? project.property("sonarCoverageExclusions") : null
          property "sonar.exclusions", project.hasProperty("sonarExclusions") ? project.property("sonarExclusions") : null
        }
      }

      if (tasks.findByName('integrationTest')) {
        if (tasks.findByName('aggregateJunitReports')) {
          tasks.sonarqube.dependsOn 'aggregateJunitReports'
        }

        sonarqube {
          properties {
            property "sonar.jacoco.reportPath", null
            property "sonar.jacoco.reportPaths", "${buildDir}/jacoco/integrationTest.exec, ${buildDir}/jacoco/test.exec"
            property "sonar.junit.reportPaths", "${buildDir}/test-results/all"
            properties["sonar.tests"] = sourceSets['test'].allJava.srcDirs.findAll { it.exists() } + sourceSets['integrationTest'].allJava.srcDirs.findAll { it.exists() }
          }
        }
      }
    }
  }

  def getTextResource(project, file) {
    project.resources.text.fromString(getClass().getClassLoader().getResource(file).text)
  }
}
