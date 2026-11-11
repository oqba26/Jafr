# Keep Model Classes
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
