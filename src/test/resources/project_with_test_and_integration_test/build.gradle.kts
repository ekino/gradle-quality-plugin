plugins {
  id("com.ekino.oss.gradle.plugin.quality")
}

repositories {
  mavenCentral()
}

testing {
  suites {
    configureEach {
      if (this is JvmTestSuite) {
        useJUnitJupiter()

        dependencies {
          implementation("org.junit.jupiter:junit-jupiter:5.9.2")
        }
      }
    }


    val integrationTest by registering(JvmTestSuite::class)
  }
}

tasks {
  named("check") {
    dependsOn(testing.suites.named("integrationTest"))
  }
}

sonarqube {
  isSkipProject = true // to avoid real sonar analysis (need a server)
}
