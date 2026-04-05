# Preserve generic signatures so Gson TypeToken keeps working after shrinking.
-keepattributes Signature

# Preserve runtime annotations used by Gson field mapping.
-keepattributes RuntimeVisibleAnnotations,RuntimeInvisibleAnnotations

# Gson can deserialize these DTOs by @SerializedName even when members are obfuscated,
# but the annotated fields themselves must remain available.
-keepclassmembers,allowobfuscation class dev.hai.emojibattery.data.volio.** {
    @com.google.gson.annotations.SerializedName <fields>;
}

-keepclassmembers,allowobfuscation class dev.hai.emojibattery.data.PadBackgroundTemplateCategory {
    @com.google.gson.annotations.SerializedName <fields>;
    <fields>;
}

# Keep TypeToken subclasses created inline so their generic type information survives.
-keep class * extends com.google.gson.reflect.TypeToken
