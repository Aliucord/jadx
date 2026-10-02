plugins {
	id("jadx-library")
}

dependencies {
	api(project(":jadx-core"))

	// show bytecode disassemble
	implementation(libs.raung.disasm)

	testImplementation(project(":jadx-core"))
}
