# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to the flags specified
# in C:\Users\<username>\AppData\Local\Android\sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.

# Keep Retrofit and Kotlinx Serialization
-keepattributes Signature
-keepattributes *Annotation*
-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
}
