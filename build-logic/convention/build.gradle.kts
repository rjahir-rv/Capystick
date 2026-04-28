plugins {
    `kotlin-dsl`
}

group = "com.capystick.buildlogic"
val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

dependencies {
    compileOnly(libs.findLibrary("android.gradlePlugin").get())
    compileOnly(libs.findLibrary("kotlin.gradlePlugin").get())
    compileOnly(libs.findLibrary("compose.compiler.gradlePlugin").get())
    compileOnly(libs.findLibrary("hilt.gradlePlugin").get())
    compileOnly(libs.findLibrary("ksp.gradlePlugin").get())
}

gradlePlugin {
    plugins {
        register("androidLibraryCompose") {
            id = "capystick.android.library.compose"
            implementationClass = "com.capystick.buildlogic.AndroidLibraryComposeConventionPlugin"
        }
        register("androidHilt") {
            id = "capystick.android.hilt"
            implementationClass = "com.capystick.buildlogic.AndroidHiltConventionPlugin"
        }
    }
}