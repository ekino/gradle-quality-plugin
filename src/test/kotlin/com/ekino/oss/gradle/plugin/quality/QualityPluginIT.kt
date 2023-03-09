package com.ekino.oss.gradle.plugin.quality

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.gradle.testkit.runner.UnexpectedBuildFailure
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.contains
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.isTrue
import strikt.java.exists
import java.io.File
import java.nio.file.Path

class QualityPluginIT {

  @TempDir
  private lateinit var tempDir: Path

  @Test
  fun `Should check project quality with only test sourceSet`() {
    val result = runTask("project_with_test", "build", "sonar")

    expectThat(result.tasks).hasSize(17)
    expectThat(result.task(":checkstyleMain")).isNotNull()
    expectThat(result.task(":checkstyleTest")).isNotNull()
    expectThat(result.task(":testCodeCoverageReport")).isNotNull()
    expectThat(result.task(":sonar")).isNotNull()

    expectThat(result.task(":checkstyleMain")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    expectThat(result.output.contains("[ant:checkstyle] [WARN]")).isTrue()
    expectThat(result.output.contains("DemoApplication.java:6:5: '{'")).isTrue()
    expectThat(result.task(":checkstyleTest")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    expectThat(result.output.contains("DemoApplicationTest.java:7:5: '{'")).isTrue()
    expectThat(result.task(":testCodeCoverageReport")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    expectThat(result.task(":sonar")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
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

    expectThat(result.tasks).hasSize(23)
    expectThat(result.task(":checkstyleMain")).isNotNull()
    expectThat(result.task(":checkstyleTest")).isNotNull()
    expectThat(result.task(":checkstyleIntegrationTest")).isNotNull()
    expectThat(result.task(":testCodeCoverageReport")).isNotNull()
    expectThat(result.task(":integrationTestCodeCoverageReport")).isNotNull()
    expectThat(result.task(":sonar")).isNotNull()

    expectThat(result.task(":checkstyleMain")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    expectThat(result.task(":checkstyleTest")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    expectThat(result.task(":checkstyleIntegrationTest")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    expectThat(result.task(":testCodeCoverageReport")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    expectThat(result.task(":integrationTestCodeCoverageReport")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    expectThat(result.task(":sonar")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
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

  @Test
  fun `Should run checkstyle task with configured tool version`() {
    val result = runTask("project_with_test", "checkStyleMain", "--info")

    expectThat(result.output) {
      contains("Running Checkstyle 9.2.1")
    }
  }

  @Test
  fun `Should run checkstyle's task with Gradle default tool version`() {
    val result = runTask("project_with_test_and_integration_test", "checkStyleMain", "--info")

    expectThat(result.output) {
      contains("Running Checkstyle 9.3")
    }
  }

  @Test
  fun `Should throw unexpected build failure exception when checkstyle config is not found`() {
    expectThrows<UnexpectedBuildFailure> { runTask("project_with_missing_checkstyle_config", "checkStyleMain", "--info") }
      .get { message }
      .isNotNull()
      .contains("Unable to find:(.*)config/missing.xml".toRegex())
  }

  private fun runTask(project: String, vararg task: String): BuildResult {
    File("src/test/resources/$project").copyRecursively(tempDir.toFile())

    return GradleRunner.create()
            .withArguments(*task, "--stacktrace")
            .withProjectDir(tempDir.toFile())
            .withPluginClasspath()
            .forwardOutput()
            .build()
  }

}
