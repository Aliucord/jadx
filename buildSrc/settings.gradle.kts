plugins {
	// Workaround for missing support for catalog version in convention Gradle plugins
	// Gradle issue: https://github.com/gradle/gradle/issues/15383
	// Plugin link: https://github.com/radoslaw-panuszewski/typesafe-conventions-gradle-plugin
	id("dev.panuszewski.typesafe-conventions") version "0.11.1"
}
