package com.ekino.oss.gradle.plugin.quality

import io.mockk.every
import io.mockk.spyk
import org.gradle.api.resources.MissingResourceException
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.*

class QualityPluginTest {

  @Test
  fun `Should contains default plugins`() {
    val project = ProjectBuilder.builder().build()

    expectThat(project.plugins).isEmpty()

    project.plugins.apply("com.ekino.oss.gradle.plugin.quality")

    expectThat(project.pluginManager.hasPlugin("java")).isTrue()
    expectThat(project.pluginManager.hasPlugin("checkstyle")).isTrue()
    expectThat(project.pluginManager.hasPlugin("org.sonarqube")).isTrue()

    expectThat(project.getTasksByName("checkstyleMain", false)).isNotEmpty()
    expectThat(project.getTasksByName("checkstyleTest", false)).isNotEmpty()
    expectThat(project.getTasksByName("sonarqube", false)).isNotEmpty()
    expectThat(project.getTasksByName("printCoverage", false)).isNotEmpty()
  }

  @Test
  fun `Should throw missing resource exception when checkstyle config is not found`() {
    val plugin = spyk<QualityPlugin>()

    every { plugin.getFilePath() } returns (null)
    expectThrows<MissingResourceException> { plugin.getCheckstyleConfig() }
            .and { message }
            .isA<String>()
            .contains("The checkstyle config file cannot be found")
  }
}
