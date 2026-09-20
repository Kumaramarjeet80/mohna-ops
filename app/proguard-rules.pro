# Proguard rules for Mohna Ops
-keepattributes *Annotation*
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class com.mohna.ops.data.model.** { *; }
