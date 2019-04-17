#  gradle quality plugin

Quality gradle plugin for Ekino projects

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

* SONARQUBE_URL
* SONARQUBE_TOKEN
* sonarCoverageExclusions
* sonarExclusions

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

then add the plugin on build.gradle

```groovy
plugins {
    id "com.ekino.oss.gradle.plugin.quality" version "1.0.0"
}
```
