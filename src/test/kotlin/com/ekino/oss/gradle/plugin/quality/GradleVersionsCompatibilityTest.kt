package com.ekino.oss.gradle.plugin.quality

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.io.File

class GradleVersionsCompatibilityTest {

    @TempDir
    lateinit var tempDir: File

    @ValueSource(strings = ["5.6.4", "6.1.1", "6.2.2"])
    @ParameterizedTest(name = "Gradle {0}")
    fun `Should work in gradle version`(gradleVersion: String) {
        val buildScript =
            """
            plugins {
                id 'com.ekino.oss.gradle.plugin.quality'
            }
            """

        File("$tempDir/build.gradle").writeText(buildScript)

        val result = GradleRunner.create()
                .withProjectDir(tempDir)
                .withGradleVersion(gradleVersion)
                .withPluginClasspath()
                .withArguments("build", "--stacktrace")
                .forwardOutput()
                .build()


        expectThat(result.task(":build")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }
}
