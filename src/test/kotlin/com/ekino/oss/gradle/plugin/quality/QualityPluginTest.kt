package com.ekino.oss.gradle.plugin.quality

import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEmpty
import strikt.assertions.isNotEmpty
import strikt.assertions.isTrue

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
  }
}
