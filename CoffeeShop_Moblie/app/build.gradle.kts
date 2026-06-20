plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    // 1. Thêm plugin của Firebase
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.coffeeshopmobile"
    compileSdk = 36 // Bạn có thể dùng compileSdk = 36 cho gọn thay vì khối release(36)

    defaultConfig {
        applicationId = "com.example.coffeeshopmobile"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    // Các thư viện mặc định của bạn (Version Catalog)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    // ==========================================
    // THƯ VIỆN BỔ SUNG CHO APP NHÂN VIÊN PHỤC VỤ
    // ==========================================

    // 1. Navigation & State (Để chuyển màn hình và quản lý dữ liệu MVVM)
    implementation("androidx.navigation:navigation-compose:2.8.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.1")
    implementation("androidx.compose.runtime:runtime-livedata")

    // 2. Firebase BoM & Realtime Database (Đồng bộ trực tiếp với Web Thu ngân/Admin)
    implementation(platform("com.google.firebase:firebase-bom:33.1.0"))
    implementation("com.google.firebase:firebase-database-ktx")

    // 3. Coil Compose (Dùng để hiển thị link ảnh Supabase trên giao diện cực mượt)
    implementation("io.coil-kt:coil-compose:2.6.0")

    // 4. Coroutines (Xử lý các tác vụ ngầm chạy bất đồng bộ)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    implementation("com.google.firebase:firebase-auth")

    // Thư viện BCrypt để giải mã mật khẩu đồng bộ với Web Spring Boot
    implementation("org.mindrot:jbcrypt:0.4")

    // Thư viện Test mặc định
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}