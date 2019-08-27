package com.ekino.oss.gradle.plugin.quality

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

import static org.junit.jupiter.api.Assertions.assertEquals

class GradleVersionsCompatibilityTest {

  @TempDir
  protected File tempDir

  @ValueSource(strings = ["5.3.1", "5.6.1"])
  @ParameterizedTest(name = "Gradle {0}")
  @DisplayName("Should work in Gradle version")
  void shouldWorkInGradleVersion(String gradleVersion) {
    def buildScript =
            """
            plugins {
                id 'com.ekino.oss.gradle.plugin.quality'
            }
            """

    new File("$tempDir/build.gradle").write buildScript

    def result = GradleRunner.create()
            .withProjectDir(tempDir)
            .withGradleVersion(gradleVersion)
            .withPluginClasspath()
            .withArguments("build", "--stacktrace")
            .forwardOutput()
            .build()

    assertEquals(TaskOutcome.SUCCESS, result.task(":build").outcome)
  }
}
