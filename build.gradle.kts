/*
 * Copyright (c) 2019 ekino (https://www.ekino.com/)
 */

import se.bjurr.gitchangelog.plugin.gradle.GitChangelogTask

plugins {
  `java-gradle-plugin`
  `kotlin-dsl`
  `maven-publish`
  signing
  id("net.researchgate.release") version "2.6.0"
  id("se.bjurr.gitchangelog.git-changelog-gradle-plugin") version "1.60"
}

repositories {
  jcenter()
}

dependencies {
  implementation("org.sonarsource.scanner.gradle:sonarqube-gradle-plugin:${property("sonarqubePluginVersion")}")

  testImplementation(gradleTestKit())
  testImplementation("org.junit.jupiter:junit-jupiter:${property("junitVersion")}")
  testImplementation("io.strikt:strikt-core:${property("striktVersion")}")
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

val sourcesJar by tasks.registering(Jar::class) {
  archiveClassifier.set("sources")
  from(sourceSets.main.get().allJava)
}

val javadocJar by tasks.registering(Jar::class) {
  archiveClassifier.set("javadoc")
  from(tasks.javadoc)
}

val printVersion by tasks.registering {
  doFirst {
    println(project.version)
  }
}

val gitChangelogTask by tasks.registering(GitChangelogTask::class) {
  file = File("CHANGELOG.md")
  templateContent = file("changelog.mustache").readText()
}

tasks {
  artifacts {
    archives(jar)
    archives(sourcesJar)
    archives(javadocJar)
  }
}

val mavenJavaPublication = "mavenJava"
val mavenPluginMarkerPublication = "mavenPluginMarker"
publishing {
  publications {
    register<MavenPublication>(mavenJavaPublication) {

      from(components["java"])

      artifact(sourcesJar.get())
      artifact(javadocJar.get())

      pom {
        name.set("Gradle quality plugin")
        description.set("Quality plugin applying some configuration for your builds (checkstyle, jacoco, sonarqube)")
        url.set("https://github.com/ekino/gradle-quality-plugin")
        licenses {
          license {
            name.set("MIT License (MIT)")
            url.set("https://opensource.org/licenses/mit-license")
          }
        }
        developers {
          developer {
            organization.set("ekino")
            organizationUrl.set("https://www.ekino.com/")
          }
        }
        scm {
          connection.set("scm:git:git://github.com/ekino/gradle-quality-plugin.git")
          developerConnection.set("scm:git:ssh://github.com:ekino/gradle-quality-plugin.git")
          url.set("https://github.com/ekino/gradle-quality-plugin")
        }
      }
    }

    //needed by sonatype oss check in staging
    register<MavenPublication>(mavenPluginMarkerPublication) {
      groupId = gradlePlugin.plugins[pluginName].id
      artifactId = "${gradlePlugin.plugins[pluginName].id}.gradle.plugin"
      pom {
        name.set("Gradle quality plugin")
        description.set("Quality plugin applying some configuration for your builds (checkstyle, jacoco, sonarqube)")
        url.set("https://github.com/ekino/gradle-quality-plugin")
        licenses {
          license {
            name.set("MIT License (MIT)")
            url.set("https://opensource.org/licenses/mit-license")
          }
        }
        developers {
          developer {
            organization.set("ekino")
            organizationUrl.set("https://www.ekino.com/")
          }
        }
        scm {
          connection.set("scm:git:git://github.com/ekino/gradle-quality-plugin.git")
          developerConnection.set("scm:git:ssh://github.com:ekino/gradle-quality-plugin.git")
          url.set("https://github.com/ekino/gradle-quality-plugin")
        }
      }
      pom.withXml {
        val dependency = asNode().appendNode("dependencies").appendNode("dependency")
        dependency.appendNode("groupId", project.group)
        dependency.appendNode("artifactId", project.name)
        dependency.appendNode("version", project.version)

      }

    }
  }
  repositories {
    maven {
      url = uri(property("ossrhUrl") as String)
      credentials {
        username = property("ossrhUsername") as String
        password = property("ossrhPassword") as String
      }
    }
  }
}

signing {
  sign(publishing.publications[mavenJavaPublication])
  //needed by sonatype oss check in staging
  sign(publishing.publications[mavenPluginMarkerPublication])
}
