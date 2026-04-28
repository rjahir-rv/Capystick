plugins {
    id("capystick.android.library.compose")
}

android {
    namespace = "com.capystick.navigation"
}


dependencies {
    // modules
    implementation(project(":feature:notepad"))
    implementation(project(":feature:collections"))
    implementation(project(":feature:settings"))
    implementation(project(":core:designsystem"))

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    
    // navigation
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
}