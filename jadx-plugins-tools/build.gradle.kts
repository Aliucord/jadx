plugins {
	id("jadx-java")
	id("jadx-library")
}

dependencies {
	api(project(":jadx-core"))

	implementation(project(":jadx-commons:jadx-app-commons"))

	implementation(libs.gson)
	implementation(libs.commons.io)

	testImplementation(libs.mockwebserver)
}
