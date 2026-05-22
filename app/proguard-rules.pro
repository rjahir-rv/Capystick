# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Glance app widgets are entered by Android framework receivers and Glance
# background sessions. Keep the widget surface and its Hilt entry point stable
# in minified release builds so Play-installed widgets can render past the
# default loading layout.
-keep class com.capystick.app.widget.** { *; }
-keep class com.capystick.domain.widget.** { *; }
-keep interface com.capystick.domain.repository.WidgetRepository { *; }
-keep class com.capystick.data.repository.WidgetRepositoryImpl { *; }
-keep class com.capystick.data.widget.** { *; }

# These enums are stored in DataStore by name; keep their names stable across
# app updates and R8 optimizations.
-keep class com.capystick.model.WidgetMode { *; }
-keep class com.capystick.designsystem.theme.ThemeOption { *; }
-keep class com.capystick.designsystem.theme.ColorPaletteOption { *; }

# Glance schedules widget rendering through WorkManager. WorkManager creates
# input mergers reflectively by class name, so keep their public constructors
# available in release builds.
-keep class * extends androidx.work.InputMerger {
    public <init>();
}
-keep class androidx.work.OverwritingInputMerger {
    public <init>();
}
-keep class androidx.work.ArrayCreatingInputMerger {
    public <init>();
}
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
