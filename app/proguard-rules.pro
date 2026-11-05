# R8 rules for the release build.
#
# The engine is pure JVM and is reached through direct Kotlin calls. A repo-wide
# search found no reflection, ServiceLoader, Parcelable or serialization entry
# points, so the old blanket `-keep class com.monaart.engine.**` is unnecessary:
# R8 keeps everything reachable from the manifest and application code.
-keepattributes *Annotation*, InnerClasses, Signature
-dontwarn org.jetbrains.annotations.**

# Hand-written codecs iterate Kotlin enum entries/values(); keep the synthetic
# enum methods that R8 might otherwise consider unused.
-keepclassmembers enum com.monaart.engine.** {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
