package com.capystick.designsystem.theme

/**
 * Represents the available theme options for the app.
 * [DYNAMIC] is only available on Android 12+ (API 31+).
 */
enum class ThemeOption {
    /** Follows the device's system light/dark setting. */
    SYSTEM,

    /** Always uses the light color scheme. */
    LIGHT,

    /** Always uses the dark color scheme. */
    DARK,

    /** Uses Material You dynamic colors (Android 12+ only). */
    DYNAMIC,
}

enum class ColorPaletteOption(
    val displayName: String,
) {
    DEFAULT(displayName = "Capystick"),

    FOREST(displayName = "Bosque"),

    OCEAN(displayName = "Océano"),

    YELLOW(displayName = "Desierto"),

    ROSE(displayName = "Coral"),

    RED(displayName = "Lava"),
}
