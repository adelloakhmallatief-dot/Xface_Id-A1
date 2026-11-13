plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.xcoder.xfaceid.core.ai"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(libs.mediapipe.tasks.vision)

    testImplementation(libs.junit)
}

// Model BlazeFace short-range (float16) diunduh SEKALI saat build, lalu disimpan di assets.
// Di perangkat, aplikasi tidak pernah memakai internet. Berkas .tflite tidak di-commit (lihat .gitignore).
// URL memakai "latest": untuk build yang bisa direproduksi, pin ke versi model tertentu nanti.
// Periksa lisensi model sebelum rilis publik.
val modelFile = layout.projectDirectory.file("src/main/assets/blaze_face_short_range.tflite").asFile
val modelUrl =
    "https://storage.googleapis.com/mediapipe-models/face_detector/blaze_face_short_range/float16/latest/blaze_face_short_range.tflite"

val downloadFaceDetectorModel = tasks.register("downloadFaceDetectorModel") {
    outputs.file(modelFile)
    onlyIf { !modelFile.exists() }
    doLast {
        modelFile.parentFile.mkdirs()
        val partFile = File(modelFile.parentFile, modelFile.name + ".part")
        java.net.URI(modelUrl).toURL().openStream().use { input ->
            partFile.outputStream().use { output -> input.copyTo(output) }
        }
        check(partFile.renameTo(modelFile)) { "Gagal menyimpan model ke ${modelFile.path}" }
    }
}

tasks.matching { it.name == "preBuild" }.configureEach {
    dependsOn(downloadFaceDetectorModel)
}
