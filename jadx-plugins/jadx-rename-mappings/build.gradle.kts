plugins {
	id("jadx-library")
}

dependencies {
	api(project(":jadx-core"))

	api(libs.mapping.io) {
		exclude("org.ow2.asm:asm")
		exclude("net.fabricmc:tiny-remapper")
	}

	testRuntimeOnly(project(":jadx-plugins:jadx-dex-input"))
	testRuntimeOnly(project(":jadx-plugins:jadx-smali-input"))
}
