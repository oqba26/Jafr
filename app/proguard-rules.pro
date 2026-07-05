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
-keep class com.oqba26.jafr.util.UpdateInfo { *; }
-keep class com.oqba26.jafr.AbjadType { *; }

# General Android keep rules
-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
