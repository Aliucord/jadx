plugins {
	id("jadx-library")
}

dependencies {
	api(project(":jadx-core"))

	implementation(project(":jadx-plugins:jadx-dex-input"))
	implementation(libs.dx)
	implementation(libs.r8)

	implementation(libs.asm)
}
