# Add project specific ProGuard rules here.

# Keep Kotlin serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keep,includedescriptorclasses class com.suvojeet.safewalk.**$$serializer { *; }
-keepclassmembers class com.suvojeet.safewalk.** {
    *** Companion;
}
-keepclasseswithmembers class com.suvojeet.safewalk.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep data models for Firebase
-keep class com.suvojeet.safewalk.data.model.** { *; }

# Keep Room entities
-keep class com.suvojeet.safewalk.data.local.db.entity.** { *; }

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**

# Retrofit
-keepattributes Signature
-keepattributes Exceptions