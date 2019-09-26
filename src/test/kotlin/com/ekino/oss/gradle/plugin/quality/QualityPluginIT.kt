package com.ekino.oss.gradle.plugin.quality

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import strikt.api.expectThat
import strikt.assertions.*
import java.io.File
import java.nio.file.Path

class QualityPluginIT {

  @TempDir
  lateinit var tempDir: Path

  @Test
  fun `Should check project quality with only test sourceSet`() {
    val result = runTask("project_with_test", "build", "sonar")

    expectThat(result.tasks).hasSize(16)
    expectThat(result.task(":checkstyleMain")).isNotNull()
    expectThat(result.task(":checkstyleTest")).isNotNull()
    expectThat(result.task(":jacocoTestReport")).isNotNull()
    expectThat(result.task(":sonarqube")).isNotNull()
    expectThat(result.task(":aggregateJunitReports")).isNull()

    expectThat(result.task(":checkstyleMain")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    expectThat(result.output.contains("[ant:checkstyle] [WARN]")).isTrue()
    expectThat(result.output.contains("DemoApplication.java:6:5: '{'")).isTrue()
    expectThat(result.task(":checkstyleTest")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    expectThat(result.output.contains("DemoApplicationTest.java:7:5: '{'")).isTrue()
    expectThat(result.task(":jacocoTestReport")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    expectThat(result.output.contains("Generating jacoco coverage report in HTML ...")).isTrue()
    expectThat(result.task(":sonarqube")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    expectThat(result.output.contains("COVERAGE: 25%")).isTrue()
    expectThat(result.task(":printCoverage")?.outcome).isEqualTo(TaskOutcome.SUCCESS)

    val jacocoTestExecFile = tempDir.resolve("build")
            .resolve("jacoco")
            .resolve("test.exec")
    expectThat(jacocoTestExecFile).exists()

    expectThat(File("$tempDir/build/jacoco").listFiles()?.size).isEqualTo(1)

    val checkstyleMainReportFile = tempDir.resolve("build")
            .resolve("reports")
            .resolve("checkstyle")
            .resolve("main.html")
    expectThat(checkstyleMainReportFile).exists()

    val checkstyleTestReportFile = tempDir.resolve("build")
            .resolve("reports")
            .resolve("checkstyle")
            .resolve("test.html")
    expectThat(checkstyleTestReportFile).exists()

    expectThat(File("$tempDir/build/reports/checkstyle").listFiles()?.size).isEqualTo(4)

    val jacocoReportDir = tempDir.resolve("build")
            .resolve("reports")
            .resolve("jacoco")
            .resolve("test")
            .resolve("html")
            .resolve("com.example.demo")
    expectThat(jacocoReportDir).exists()

    val jacocoUtilsReportDir = tempDir.resolve("build")
            .resolve("reports")
            .resolve("jacoco")
            .resolve("test")
            .resolve("html")
            .resolve("com.example.demo.utils")
    expectThat(jacocoUtilsReportDir).not().exists() // Excluded from coverage
  }

  @Test
  fun `Should check project quality with test and integrationTest sourceSets`() {
    val result = runTask("project_with_test_and_integration_test", "build", "sonar")

    expectThat(result.tasks).hasSize(22)
    expectThat(result.task(":checkstyleMain")).isNotNull()
    expectThat(result.task(":checkstyleTest")).isNotNull()
    expectThat(result.task(":checkstyleIntegrationTest")).isNotNull()
    expectThat(result.task(":jacocoTestReport")).isNotNull()
    expectThat(result.task(":sonarqube")).isNotNull()
    expectThat(result.task(":aggregateJunitReports")).isNotNull()

    expectThat(result.task(":checkstyleMain")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    expectThat(result.task(":checkstyleTest")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    expectThat(result.task(":checkstyleIntegrationTest")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    expectThat(result.task(":jacocoTestReport")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    expectThat(result.task(":sonarqube")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    expectThat(result.task(":aggregateJunitReports")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    expectThat(result.output.contains("COVERAGE: 0%")).isTrue()
    expectThat(result.task(":printCoverage")?.outcome).isEqualTo(TaskOutcome.SUCCESS)

    val jacocoTestExecFile = tempDir.resolve("build")
            .resolve("jacoco")
            .resolve("test.exec")
    expectThat(jacocoTestExecFile).exists()

    val jacocoIntegrationTestExecFile = tempDir.resolve("build")
            .resolve("jacoco")
            .resolve("integrationTest.exec")
    expectThat(jacocoIntegrationTestExecFile).exists()

    expectThat(File("$tempDir/build/jacoco").listFiles()?.size).isEqualTo(2)

    val checkstyleMainReportFile = tempDir.resolve("build")
            .resolve("reports")
            .resolve("checkstyle")
            .resolve("main.html")
    expectThat(checkstyleMainReportFile).exists()

    val checkstyleTestReportFile = tempDir.resolve("build")
            .resolve("reports")
            .resolve("checkstyle")
            .resolve("test.html")
    expectThat(checkstyleTestReportFile).exists()

    val checkstyleIntegrationTestReportFile = tempDir.resolve("build")
            .resolve("reports")
            .resolve("checkstyle")
            .resolve("integrationTest.html")
    expectThat(checkstyleIntegrationTestReportFile).exists()

    expectThat(File("$tempDir/build/reports/checkstyle").listFiles()?.size).isEqualTo(6)

    val jacocoReportDir = tempDir.resolve("build")
            .resolve("reports")
            .resolve("jacoco")
            .resolve("test")
            .resolve("html")
    expectThat(jacocoReportDir).exists()
  }

  private fun runTask(project: String, vararg task: String): BuildResult {
    File("src/test/resources/$project").copyRecursively(tempDir.toFile())

    return GradleRunner.create()
            .withArguments(*task, "--stacktrace")
            .withProjectDir(tempDir.toFile())
            .withTestKitDir(tempDir.toFile())
            .withPluginClasspath()
            .forwardOutput()
            .build()
  }

}
