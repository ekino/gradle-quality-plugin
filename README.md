#  gradle quality plugin

Quality gradle plugin for ekino projects

[![GitHub (pre-)release](https://img.shields.io/github/release/ekino/gradle-quality-plugin.svg)](https://github.com/ekino/gradle-quality-plugin/releases)
[![GitHub license](https://img.shields.io/github/license/ekino/gradle-quality-plugin.svg)](https://github.com/ekino/gradle-quality-plugin/blob/master/LICENSE.md)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=ekino_gradle-quality-plugin&metric=alert_status)](https://sonarcloud.io/dashboard?id=ekino_gradle-quality-plugin)

## Overview

This plugin configures:

* Check style (via [checkstyle](http://checkstyle.sourceforge.net/))
* Code coverage (via [Jacoco](http://www.jacoco.org/))
* Continuous code quality (via [SonarQube](https://www.sonarqube.org/))

## Requirements

Gradle 6.0 and JDK 8 are required.

## Checkstyle configuration

This plugin checks that import had 3 groups in the below order:

* com
* java
* javax

and after that a latest group with static import

Be careful to configure your IDE code style with the same configuration to avoir errors.

You can override checkstyle version using a dedicated configuration.

Note: The default checkstyle.xml is compatible with 8.24 or later 

```kotlin
checkstyle {
    toolVersion = "8.29"
    configFile = file("${project.rootDir}/config/checkstyle.xml")
}
```

## Sonar Configuration

These values can be set in project properties:

* **SONARQUBE_URL**: Sonarqube server URL. Ex : `http://localhost:9000`
* **SONARQUBE_TOKEN**: login or authentication token of a SonarQube user with Execute Analysis permission on the project
* **sonarCoverageExclusions**: source files to exclude from code coverage (java extension, wildcard allowed, comma separator). Ex : `**/*Properties.java, **/*Constants.java`
* **sonarExclusions**: source files to exclude from analysis (java extension, wildcard allowed, comma separator). Ex : `**/*Properties.java, **/*Constants.java`

Or you can set it in manuel mode on your CI pipeline (because you don't want that each developer publish to sonar)

## Usage

Add the plugin in your Gradle build script:

Groovy
```groovy
plugins {
    id "com.ekino.oss.gradle.plugin.quality" version "0.0.2"
}
```

Kotlin
```kotlin
plugins {
    id("com.ekino.oss.gradle.plugin.quality") version "0.0.2"
}
```

## For contributors

### Build

This will create the JAR and run the tests

    ./gradlew build

### Test on a local project 

Add to your local project in the settings.gradle as described in [Gradle doc](https://docs.gradle.org/nightly/userguide/testing_gradle_plugins.html#verify-url-cmd-output)
includeBuild '[path/to/the/gradle/plugin]'

### Publish

This will upload the plugin to Nexus repository

    ./gradlew build publish
