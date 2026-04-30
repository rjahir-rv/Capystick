plugins {
    id("capystick.android.library.compose")
    id("capystick.android.hilt")
}

android {
    namespace = "com.capystick.collections"
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:domain"))
    implementation(project(":core:designsystem"))
    
    implementation(libs.hilt.navigation.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
}