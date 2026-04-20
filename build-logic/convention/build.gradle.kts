plugins {
    `kotlin-dsl`
}

group = "com.capystick.buildlogic"
val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

dependencies {
    compileOnly(libs.findLibrary("android.gradlePlugin").get())
    compileOnly(libs.findLibrary("kotlin.gradlePlugin").get())
    compileOnly(libs.findLibrary("compose.compiler.gradlePlugin").get())
}

gradlePlugin {
    plugins {
        register("androidLibraryCompose") {
            id = "capystick.android.library.compose"
            implementationClass = "com.capystick.buildlogic.AndroidLibraryComposeConventionPlugin"
        }
    }
}