/*
 * Copyright (c) 2019 ekino (https://www.ekino.com/)
 */

package com.ekino.oss.gradle.plugin.quality.task

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File

open class PrintCoverageTask : DefaultTask() {
    @Input
    lateinit var htmlJacocoReport: String

    @TaskAction
    fun printCoverage() {
        File(htmlJacocoReport).takeIf { it.exists() }?.apply {
            ">Total<.+>(?<coverage>[0-9]+) ?%<".toRegex().find(readText())?.let {
                println("COVERAGE: ${it.groups["coverage"]?.value}%")
            }
        }
    }
}
