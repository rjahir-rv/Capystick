plugins {
    id("capystick.android.library.compose")
}

android {
    namespace = "com.capystick.notepad"
}


dependencies {
    implementation(project(":core:designsystem"))
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    implementation(libs.richeditor.compose)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}