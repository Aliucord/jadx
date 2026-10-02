plugins {
	id("jadx-library")
}

dependencies {
	api(project(":jadx-core"))

	// TODO: finish own smali printer
	implementation(libs.smali.baksmali) {
		exclude(group = "com.beust", module = "jcommander") // exclude old jcommander namespace
	}
	implementation(libs.guava.jre) // force the latest version for smali

	// compile smali files in tests
	testImplementation(libs.smali) {
		exclude(group = "com.beust", module = "jcommander") // exclude old jcommander namespace
	}
}
