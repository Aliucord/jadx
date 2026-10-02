plugins {
	id("jadx-library")
}

dependencies {
	implementation(project(":jadx-core"))

	implementation(libs.gson)

	testRuntimeOnly(project(":jadx-plugins:jadx-dex-input"))
	testRuntimeOnly(project(":jadx-plugins:jadx-smali-input"))
}
