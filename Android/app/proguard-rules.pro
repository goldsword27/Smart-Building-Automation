# ProGuard yapılandırma dosyasının amacı ve kullanımı hakkında genel açıklamalar.
# proguardFiles ayarını build.gradle dosyasında kontrol edebilirsiniz.
# Daha fazla bilgi için ProGuard dokümantasyonuna bakabilirsiniz:
#   http://developer.android.com/guide/developing/tools/proguard.html

# Eğer projenizde WebView ile JavaScript kullanıyorsanız, aşağıdaki satırı açarak
# WebView JavaScript arayüzü sınıfının tam adını belirtin:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Hata ayıklama yığın izleri için satır numarası bilgisini korumak istiyorsanız,
# aşağıdaki satırı açın:
-keepattributes SourceFile,LineNumberTable

# Satır numarası bilgisini koruyorsanız, orijinal kaynak dosya adını gizlemek için
# aşağıdaki satırı açın:
-renamesourcefileattribute SourceFile
# ------------------------
# Uygulamanız için özel Proguard kuralları

# -keep direktifi belirli sınıfları veya paketleri korumanızı sağlar
-keep class com.tbmyo.otomasyon.** { *; }

# Retrofit ve Gson ile ilgili kurallar
-keep class com.squareup.** { *; }  # SquareUp (Retrofit ve Gson) sınıflarını korur
-dontwarn com.squareup.okhttp3.**  # OkHttp ile ilgili uyarıları yok sayar
-dontwarn retrofit2.**  # Retrofit ile ilgili uyarıları yok sayar
-keepattributes Signature  # İmza bilgilerini korur
-keepattributes *Annotation*  # Annotation bilgilerini korur

# AndroidX ve Material ile ilgili kurallar
-keep class androidx.** { *; }  # AndroidX sınıflarını korur
-keep class com.google.android.material.** { *; }  # Material Design sınıflarını korur

# Espresso ve testlerle ilgili kurallar
-keep class androidx.test.espresso.** { *; }  # Espresso test sınıflarını korur
-keep class androidx.test.ext.junit.** { *; }  # JUnit test sınıflarını korur

# Diğer kurallar ve özel gereksinimler
-keepattributes Signature  # İmza bilgilerini korur
-keepattributes *Annotation*  # Annotation bilgilerini korur
