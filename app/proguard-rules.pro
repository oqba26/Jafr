# Gson rules
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class com.google.gson.reflect.TypeToken
-keep class * extends com.google.gson.reflect.TypeToken
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Keep Model Classes (for Gson serialization/deserialization)
-keep class com.oqba26.jafr.model.** { *; }
-keep class com.oqba26.jafr.AbjadType { *; }

# kotlinx.serialization rules (UpdateInfo)
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keep,includedescriptorclasses class com.oqba26.jafr.**$$serializer { *; }
-keepclassmembers class com.oqba26.jafr.** {
    *** Companion;
}
-keepclasseswithmembers class com.oqba26.jafr.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Ktor: slf4j به‌صورت اختیاری استفاده می‌شود و در کلاس‌پس نیست
-dontwarn org.slf4j.**

# General Android keep rules
-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
