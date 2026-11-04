# Compose + Kotlin serialization-friendly rules for the release build.
-keepattributes *Annotation*, InnerClasses, Signature
-dontwarn org.jetbrains.annotations.**
-keep class com.monaart.engine.** { *; }
-keepclassmembers class com.monaart.studio.data.document.** { *; }
