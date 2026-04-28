plugins {
    id("capystick.android.library.compose")
}

android {
    namespace = "com.capystick.collections"
}


dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
}