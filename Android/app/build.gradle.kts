plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
}

android {
    namespace = "com.tbmyo.otomasyon"
    compileSdk = 34

    buildFeatures {
        viewBinding = true //dikkat et
        dataBinding = true //yerler seni
    }

    defaultConfig {
        applicationId = "com.tbmyo.otomasyon"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "27.44522"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true //proguard kodları karmaşıklaştırır, tersine mühendislik saldırılarından korur.
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro" //dosyayı explorerden; projenin ana dizininde bulabilirsin veya direkt gradle scripts içerisinde
                //zaten türkçeleştirdim her şeyi ayarladım.
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation("com.squareup.retrofit2:retrofit:2.9.0") // HTTP istekleri için Retrofit kütüphanesi
    implementation("com.squareup.retrofit2:converter-gson:2.9.0") // JSON verilerini dönüştürmek için Gson dönüştürücüsü
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.0") // HTTP istek ve yanıtlarını loglamak için interceptor
    implementation("androidx.core:core-ktx:1.13.1") // Android çekirdek kütüphanesi için Kotlin uzantıları
    implementation("androidx.appcompat:appcompat:1.7.0") // Android uygulamalarında geriye dönük uyumluluk sağlayan kütüphane
    implementation("com.google.android.material:material:1.12.0") // Material Design bileşenleri
    implementation("androidx.constraintlayout:constraintlayout:2.1.4") // Esnek ve-veya karmaşık layoutlar oluşturmak
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7") // Fragmentlar arasında gezinme
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7") // Navigation UI bileşenleri
    implementation("androidx.recyclerview:recyclerview:1.3.2") // Liste ve grid verilerini göstermek için RecyclerView
    testImplementation("junit:junit:4.13.2") // Birim testleri
    androidTestImplementation("androidx.test.ext:junit:1.2.1") // Android birim testleri
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1") // UI testleri
    implementation("androidx.security:security-crypto:1.1.0-alpha06") // Verileri şifrelemek ve güvenli hale getirmek için crypto librarysi
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0") //grafik için
}


