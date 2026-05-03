plugins {
    id("capystick.android.library.compose")
}

android {
    namespace = "com.capystick.domain"
}

dependencies {
    implementation(project(":core:model"))
    implementation(libs.kotlinx.coroutines.core)
}
