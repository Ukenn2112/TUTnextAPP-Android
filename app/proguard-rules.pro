# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.

# Retrofit
-keepattributes Signature
-keepattributes *Annotation*
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# Gson - keep all serialized data model classes
-keep class com.meikenn.tama.data.model.** { *; }
-keep class com.meikenn.tama.domain.model.** { *; }

# Gson - TypeToken: critical for R8 version 3.0+ compatibility.
# Retains generic signatures of TypeToken and its subclasses so that
# Gson can correctly resolve generic type parameters at runtime.
-keep,allowobfuscation,allowshrinking class com.google.gson.reflect.TypeToken
-keep,allowobfuscation,allowshrinking class * extends com.google.gson.reflect.TypeToken

# Gson - TypeAdapter and factory interfaces
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Gson - keep fields annotated with @SerializedName even when the class is obfuscated
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Gson - suppress warnings from internal sun.misc APIs
-dontwarn sun.misc.**

# Google Tink / EncryptedSharedPreferences
-dontwarn com.google.errorprone.annotations.CanIgnoreReturnValue
-dontwarn com.google.errorprone.annotations.CheckReturnValue
-dontwarn com.google.errorprone.annotations.Immutable
-dontwarn com.google.errorprone.annotations.RestrictedApi
