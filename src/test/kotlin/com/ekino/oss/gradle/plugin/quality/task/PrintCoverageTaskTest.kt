package com.ekino.oss.gradle.plugin.quality.task

import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.io.File

internal class PrintCoverageTaskTest {

    @ParameterizedTest
    @ValueSource(strings = ["en", "fr"])
    fun `should find coverage from JaCoCo reports`(local: String) {
        // Given
        val project = ProjectBuilder.builder().build()

        project.plugins.apply("com.ekino.oss.gradle.plugin.quality")

        val reportContent = File(javaClass.getResource("/jacoco-reports/$local.index.html").toURI()).readText()

        val printCoverageTask = project.getTasksByName("printCoverage", false).first() as PrintCoverageTask

        // When
        val matchResult = printCoverageTask.coverageRegex.find(reportContent)

        // Then
        expectThat(matchResult!!.groups["coverage"]!!.value).isEqualTo("75")
    }
}
