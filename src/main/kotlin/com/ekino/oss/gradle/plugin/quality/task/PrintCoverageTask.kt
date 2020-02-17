/*
 * Copyright (c) 2020 ekino (https://www.ekino.com/)
 */

package com.ekino.oss.gradle.plugin.quality.task

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File

open class PrintCoverageTask : DefaultTask() {
    @Input
    lateinit var htmlJacocoReport: String

    @Input
    var coverageRegex = ">Total<.+>(?<coverage>[0-9]+)[\\s\\u00A0]?%<".toRegex()

    @TaskAction
    fun printCoverage() {
        File(htmlJacocoReport).takeIf { it.exists() }?.apply {
            coverageRegex.find(readText())?.let {
                println("COVERAGE: ${it.groups["coverage"]?.value}%")
            }
        }
    }
}
