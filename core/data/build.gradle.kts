plugins {
    id("capystick.android.library.compose")
    id("capystick.android.hilt")
}

android {
    namespace = "com.capystick.core.data"
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:database"))
    implementation(project(":core:domain"))
}
