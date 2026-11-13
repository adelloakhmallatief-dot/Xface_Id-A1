// Root build file: hanya mendeklarasikan plugin, tanpa menerapkannya.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.compose) apply false
}
