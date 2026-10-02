plugins {
	id("jadx-library")
	id("jadx-kotlin")
}

dependencies {
	api(project(":jadx-core"))

	testImplementation(
		project
			.project(":jadx-core")
			.sourceSets
			.getByName("test")
			.output,
	)
	testImplementation(libs.commons.lang3)

	testRuntimeOnly(project(":jadx-plugins:jadx-smali-input"))
}
