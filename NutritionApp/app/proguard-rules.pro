# Project specific ProGuard / R8 rules for BSNutrition Release

-keepattributes Signature
-keepattributes *Annotation*

# Kotlinx Serialization & Retrofit DTOs
-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
    @kotlinx.serialization.Serializable class *;
}
-keep class com.bsnutrition.app.core.network.dto.** { *; }

# Room Database Entities & DAOs
-keep class androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keepclassmembers class * {
    @androidx.room.Dao *;
    @androidx.room.Insert *;
    @androidx.room.Query *;
    @androidx.room.Update *;
    @androidx.room.Delete *;
}

# Google ML Kit (Barcode Scanning & Text Recognition)
-keep class com.google.mlkit.vision.** { *; }
-keep class com.google.android.gms.vision.** { *; }

# Health Connect
-keep class androidx.health.connect.client.** { *; }
-keep class androidx.health.connect.client.records.** { *; }

# CameraX
-keep class androidx.camera.core.** { *; }
-keep class androidx.camera.lifecycle.** { *; }
-keep class androidx.camera.view.** { *; }
