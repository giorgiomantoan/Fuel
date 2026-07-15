# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /sdk/tools/proguard/proguard-android.txt

# Keep Room entities
-keep class com.example.fuel.data.local.entity.** { *; }

# Keep osmdroid
-keep class org.osmdroid.** { *; }

# Keep OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
