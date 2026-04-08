# ProGuard Rules for AgroControl

# Keep Retrofit models
-keep class com.agrocontrol.domain.model.** { *; }
-keep class com.agrocontrol.data.repository.** { *; }

# Keep Room entities
-keep class com.agrocontrol.data.local.entities.** { *; }
-keepclassmembers class com.agrocontrol.data.local.entities.** {
    <fields>;
    <init>(...);
}

# Keep Gson serialization names
-keepattributes Annotation
-keepattributes Signature
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Compose / Coroutines rules implied by AGP, but we ensure Hilt/WorkManager aren't broken
-keep class androidx.hilt.work.** { *; }
-keep class com.agrocontrol.domain.worker.** { *; }
