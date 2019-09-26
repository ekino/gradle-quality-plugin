#  gradle quality plugin

Quality gradle plugin for Ekino projects

[![Build Status](https://travis-ci.org/ekino/gradle-quality-plugin.svg?branch=master)](https://travis-ci.org/ekino/gradle-quality-plugin)
[![GitHub (pre-)release](https://img.shields.io/github/release/ekino/gradle-quality-plugin.svg)](https://github.com/ekino/gradle-quality-plugin/releases)
[![GitHub license](https://img.shields.io/github/license/ekino/gradle-quality-plugin.svg)](https://github.com/ekino/gradle-quality-plugin/blob/master/LICENSE.md)

## Overview

This plugin configures :

* Check style (via [checkstyle](http://checkstyle.sourceforge.net/))
* Code coverage (via [Jacoco](http://www.jacoco.org/))
* Continuous code quality (via [SonarQube](https://www.sonarqube.org/))

### Checkstyle configuration

This plugin checks that import had 3 groups in the below order :

* com
* java
* javax

and after that a latest group with static import

Be careful to configure your IDE code style with the same configuration to avoir errors.

### Sonar Configuration

These values can be set in project properties :

* **SONARQUBE_URL** : Sonarqube server URL. Ex : `http://localhost:9000`
* **SONARQUBE_TOKEN** : login or authentication token of a SonarQube user with Execute Analysis permission on the project
* **sonarCoverageExclusions** : source files to exclude from code coverage (java extension, wildcard allowed, comma separator). Ex : `**/*Properties.java, **/*Constants.java`
* **sonarExclusions** : source files to exclude from analysis (java extension, wildcard allowed, comma separator). Ex : `**/*Properties.java, **/*Constants.java`

Or you can set it in manuel mode on your CI pipeline (because you don't want that each developer publish to sonar)


## Build

This will create the JAR and run the tests

    ./gradlew build

## Publish locally

This will publish the JAR in your local Maven repository

    ./gradlew publishToMavenLocal

## Publish

This will upload the plugin to Nexus repository

    ./gradlew build publish

## Usage

To use this plugin add the maven repository on settings.gradle (must be the first block of the file)

```groovy
pluginManagement {
  repositories {
    mavenCentral()
  }
}    
```

Or for SNAPSHOT versions :

```groovy
pluginManagement {
  repositories {
    maven {
      url 'https://oss.sonatype.org/content/repositories/snapshots/'
    }
  }
}
```

Then add the plugin on your Gradle build script:

Groovy
```groovy
plugins {
    id "com.ekino.oss.gradle.plugin.quality" version "0.0.1"
}
```

Kotlin
```kotlin
plugins {
    id("com.ekino.oss.gradle.plugin.quality") version "0.0.1"
}
```

### Requirements

Gradle 5.3.1 and JDK 8 are required.


You can override checkstyle version using a dedicated configuration (default is 8.24)

Note: The default checkstyle.xml is compatible with 8.24 or later 
```kotlin
checkstyle {
    toolVersion = "8.20"
    configFile = file("${project.rootDir}/config/checkstyle.xml")
}
```
