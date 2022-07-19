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

    @ValueSource(strings = ["6.9.2", "7.3.3"])
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
                .withJaCoCo()
                .forwardOutput()
                .build()


        expectThat(result.task(":build")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }

    private fun GradleRunner.withJaCoCo(): GradleRunner {
        val s = javaClass.classLoader.getResourceAsStream("testkit-gradle.properties")?.bufferedReader()?.readText() ?: ""
        File(projectDir, "gradle.properties").appendText(s)
        return this
    }
}
