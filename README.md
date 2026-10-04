# Vehicle Maintenance Pro

Vehicle Maintenance Pro adalah aplikasi Android native untuk mengelola riwayat perawatan kendaraan, reminder servis, konsumsi BBM, pengeluaran, dan laporan kendaraan.

## Status

Tahap saat ini: aplikasi lokal siap dipakai dengan desain cockpit modern untuk kendaraan, reminder servis, aktivitas, biaya, statistik, dan pengaturan profil ringan.

Yang sudah tersedia:

- Android native Kotlin + Jetpack Compose.
- Clean Architecture dengan pemisahan `presentation`, `domain`, `data`, `core`, dan `di`.
- MVVM, Repository Pattern, Use Case, Hilt, Room, Flow, Coroutines, Material 3, Navigation Compose.
- Dashboard cockpit baru untuk kendaraan bensin: light mode paksa, canvas abu muda, panel putih floating, aksen biru lembut, kendaraan besar, ringkasan odometer/BBM/servis, biaya bulanan, dan reminder.
- Tab kendaraan untuk tambah mobil/motor, memilih kendaraan aktif, dan arsip kendaraan.
- Form tambah kendaraan memakai dropdown modern untuk merek/model dan mengisi transmisi serta jenis BBM otomatis dari model kendaraan yang dipilih.
- Tab Servis memisahkan Riwayat dan Jadwal. Catatan membawa kategori, tanggal, KM, lokasi, biaya, dan pekerjaan per komponen; riwayat dapat difilter kategori.
- Estimasi per komponen memakai catatan terakhir dan interval KM/waktu. Acuan bawaan tersedia untuk Honda PCX 160 generasi 2021–2024; model lain memakai interval pilihan pengguna. Pemeriksaan tidak mereset jadwal penggantian. Lihat [pedoman Honda PCX halaman 80–81](https://www.wahanahonda.com/assets/upload/buku_manual/honda-pcx.pdf).
- Tab statistik untuk filter kendaraan, biaya per bulan, tren enam bulan, dan jadwal perawatan berdasarkan tanggal maupun kilometer. Total biaya tidak dibatasi jumlah aktivitas terbaru.
- Tab Profil untuk menyimpan nama pengguna, mengelola kendaraan, memperbarui odometer, dan membuka pengaturan notifikasi Android.
- State halaman dipertahankan selama Activity hidup. Form ditutup setelah simpan berhasil; saat gagal, input tetap tersedia dan error ditampilkan.
- Room database untuk kendaraan, tipe kendaraan, reminder, activity log, dan migrasi schema biaya aktivitas.
- Unit test use case, ViewModel test, dan Compose UI smoke test.

Yang belum diaktifkan:

- Firebase Analytics dan Crashlytics menunggu `google-services.json`.
- Notifikasi sudah diperiksa harian melalui WorkManager; alarm pada jam yang tepat belum diterapkan.
- PIN/biometric dan encrypted settings menunggu implementasi fitur keamanan.
- Cloud backup/sync disiapkan secara arsitektur, belum dihubungkan ke provider.

## Target Android

- `minSdk`: 26
- `targetSdk`: 36
- `compileSdk`: 37, hanya untuk kompatibilitas AndroidX terbaru

Catatan: per 17 Juli 2026, Android 16/API 36 adalah stable terbaru. Android 17/API 37 masih beta, sehingga target rilis awal tetap API 36.

## Build

Project memakai Gradle Wrapper. Gunakan JDK 17 atau lebih baru.

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:testDebugUnitTest
```

Jika Java global masih Java 8, jalankan dengan `JAVA_HOME` ke JDK Android Studio yang valid.

Untuk pengujian yang terpisah dari aplikasi `.debug`, gunakan `-PdebugApplicationIdSuffix=.qc` saat build. APK ini berlabel **Vehicle Maintenance Pro QC** dan menyimpan data sendiri. Tes perjalanan yang membuat contoh data hanya berjalan pada package `.qc`.

## Dokumentasi

- `Architecture.md`: keputusan arsitektur dan alasan teknis.
- `Database.md`: schema database tahap berjalan.
- `Changelog.md`: riwayat perubahan.
