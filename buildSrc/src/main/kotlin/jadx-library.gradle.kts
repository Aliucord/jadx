plugins {
	id("jadx-java")
	id("java-library")
	id("maven-publish")
	id("signing")
}

val jadxVersion: String by rootProject.extra

group = "com.aliucord.jadx"
version = jadxVersion

java {
	withJavadocJar()
	withSourcesJar()
}

publishing {
	publications {
		create<MavenPublication>("mavenJava") {
			artifactId = project.name
			from(components["java"])
			versionMapping {
				usage("java-api") {
					fromResolutionOf("runtimeClasspath")
				}
				usage("java-runtime") {
					fromResolutionResult()
				}
			}
			pom {
				name.set(project.name)
				description.set(project.description ?: "Dex to Java decompiler")
				url.set("https://github.com/skylot/jadx")
				licenses {
					license {
						name.set("The Apache License, Version 2.0")
						url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
					}
				}
				developers {
					developer {
						id.set("skylot")
						name.set("Skylot")
						email.set(project.properties["libEmail"].toString())
						url.set("https://github.com/skylot")
					}
				}
				scm {
					connection.set("scm:git:git://github.com/skylot/jadx.git")
					developerConnection.set("scm:git:ssh://github.com:skylot/jadx.git")
					url.set("https://github.com/skylot/jadx")
				}
			}
		}
	}
	repositories {
		maven {
			val releasesRepoUrl = uri("https://maven.aliucord.com/releases")
			val snapshotsRepoUrl = uri("https://maven.aliucord.com/snapshots")
			val isSnapshot = version.toString().endsWith("SNAPSHOT")
			url = if (isSnapshot) snapshotsRepoUrl else releasesRepoUrl
			credentials {
				username = System.getenv(if (isSnapshot) "MAVEN_USERNAME" else "MAVEN_RELEASE_USERNAME")
				password = System.getenv(if (isSnapshot) "MAVEN_PASSWORD" else "MAVEN_RELEASE_PASSWORD")
			}
		}
	}
}

signing {
	isRequired = gradle.taskGraph.hasTask("publish")
	sign(publishing.publications["mavenJava"])
}


tasks.javadoc {
	val stdOptions = options as StandardJavadocDocletOptions
	stdOptions.addBooleanOption("html5", true)
	// disable 'missing' warnings
	stdOptions.addStringOption("Xdoclint:all,-missing", "-quiet")
}
