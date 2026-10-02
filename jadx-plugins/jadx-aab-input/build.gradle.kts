plugins {
	id("jadx-library")
}

dependencies {
	compileOnly(project(":jadx-core"))

	implementation(libs.aapt2.proto)
	implementation(libs.protobuf.java)

	implementation(libs.bundletool) {
		// All of this is unnecessary for parsing BundleConfig.pb except for protobuf
		exclude(group = "com.android.tools.build")
		exclude(group = "com.google.protobuf")
		exclude(group = "com.google.guava")
		exclude(group = "org.bitbucket.b_c")
		exclude(group = "org.slf4j")
		exclude(group = "com.google.auto.value")
		exclude(group = "com.google.dagger")
		exclude(group = "com.google.errorprone")
	}
}
