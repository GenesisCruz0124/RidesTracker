-keepattributes *Annotation*
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class com.ridestracker.data.remote.** { *; }
-keep class com.ridestracker.domain.model.** { *; }
-dontwarn okhttp3.**
-dontwarn retrofit2.**
