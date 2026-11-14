# XFace ID

Autentikasi wajah Android, lokal dan offline. Positioning: **quick unlock** berbasis kamera 2D untuk kenyamanan, bukan pengganti lockscreen sistem.

Developer: XCoder

## Status

Fase 2 dari 8: deteksi wajah (MediaPipe BlazeFace) + alur registrasi.

Yang sudah ada: preview kamera depan, deteksi wajah real-time, kotak overlay, gerbang kualitas
(satu wajah, ukuran, posisi tengah, menghadap depan), dan pengambilan 5 sampel wajah di memori.
Belum ada: embedding/recognition, anti-spoofing, penyimpanan (Fase 3-4).

Build pertama butuh internet untuk mengunduh model `blaze_face_short_range.tflite` ke
`core/ai/src/main/assets/` (otomatis lewat task Gradle). Aplikasi di perangkat tetap 100% offline.

## Mengunduh APK

Setiap build hijau di `main` menerbitkan APK otomatis ke rilis **Nightly**. Tidak perlu login atau ekstrak zip.

| Edisi | Unduh langsung |
|---|---|
| Standard (disarankan) | [XFaceID-standard-debug.apk](https://github.com/adelloakhmallatief-dot/Xface_Id-A1/releases/download/nightly/XFaceID-standard-debug.apk) |
| Lite | [XFaceID-lite-debug.apk](https://github.com/adelloakhmallatief-dot/Xface_Id-A1/releases/download/nightly/XFaceID-lite-debug.apk) |

Semua berkas ada di halaman [Releases](https://github.com/adelloakhmallatief-dot/Xface_Id-A1/releases/tag/nightly). Cadangan: artifact `xface-id-apk` di tab **Actions** (berupa zip).

Catatan:
- APK ini berjenis **debug**, hanya untuk uji coba di HP Android (iPhone tidak bisa memasang APK).
- Saat memasang, Android akan meminta izin "pasang dari sumber tak dikenal" untuk browser atau app File yang kamu pakai.
- Tiap build ditandatangani kunci debug yang berbeda. Jika pemasangan update gagal, **hapus dulu versi lama** lalu pasang yang baru.

## Struktur

```
app/               flavor standard + lite, MainActivity, izin kamera
core/camera/       CameraPreview (CameraX)
core/ai/           deteksi (Fase 2, ada), recognition + anti-spoofing (Fase 3)
core/auth/         (Fase 4)    authentication engine
core/security/     (Fase 4)    Android Keystore, PIN
core/database/     (Fase 2)    Room, embedding terenkripsi
ui/standard/       (Fase 6)    UI premium + theme engine .xftheme
ui/lite/           (Fase 7)    UI minimalis
feature/xpassword/ (Fase 5)    opsional, opt-in, dengan layar peringatan
```

Modul bertanda fase belum di-include di `settings.gradle.kts`. Tambahkan saat fasenya dikerjakan.

## Versi (diverifikasi 7 Nov 2026)

| Komponen | Versi |
|---|---|
| Android Gradle Plugin | 9.0.1 (butuh Gradle 9.1.0+, JDK 17+) |
| Kotlin | 2.2.10 (built-in AGP 9, tanpa plugin kotlin-android) |
| Compose (ui/foundation) | 1.11.4 |
| Material3 | 1.4.0 |
| CameraX | 1.6.2 |
| Activity Compose | 1.13.0 |
| Core KTX | 1.18.0 |
| Lifecycle | 2.10.0 |
| MediaPipe tasks-vision | 0.10.35 (**belum terverifikasi** untuk Maven Android, cek saat sync) |
| JUnit | 4.13.2 |

minSdk 26, compileSdk/targetSdk 36.

## Menjalankan

1. Buka folder ini di Android Studio versi terbaru (yang mendukung AGP 9.0).
2. Jika `gradle/wrapper` belum ada, jalankan `gradle wrapper --gradle-version 9.1.0` atau biarkan Android Studio membuatnya.
3. Sync, pilih variant `standardDebug` atau `liteDebug`, jalankan di perangkat nyata (emulator kamera depan sering tidak representatif).

Hasil yang diharapkan: layar minta izin kamera, lalu preview kamera depan penuh layar.

## Prinsip

- Tanpa cloud, tanpa API eksternal.
- Frame kamera tidak pernah ditulis ke disk.
- Lite dan Standard memakai core engine yang sama.

## Lisensi

Kode XFace ID dilisensikan di bawah [Apache License 2.0](LICENSE). Hak cipta 2026 XCoder.

Berkas model AI (misalnya detektor wajah `.tflite`) tidak ikut lisensi ini dan tetap memakai lisensi penerbit aslinya. Lihat [NOTICE](NOTICE). Lisensi bobot model recognition dan anti-spoofing akan diperiksa di Fase 3 sebelum dipakai.
