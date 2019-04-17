package com.ekino.oss.gradle.plugin.quality

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertNotNull
import static org.junit.jupiter.api.Assertions.assertTrue

class QualityPluginTest {

  @Test
  void shouldContainsDefaultPlugins() {
    Project project = ProjectBuilder.builder().build()

    assertEquals(0, project.getExtensions().getPlugins().size())
    project.apply plugin: 'com.ekino.oss.gradle.plugin.quality'

    assertTrue(project.pluginManager.hasPlugin('checkstyle'))
    assertTrue(project.pluginManager.hasPlugin('jacoco'))
    assertTrue(project.pluginManager.hasPlugin('org.sonarqube'))
    assertNotNull(project.getTasksByName('checkstyleMain', false))
    assertNotNull(project.getTasksByName('checkstyleTest', false))
    assertNotNull(project.getTasksByName('sonar', false))
  }
}
