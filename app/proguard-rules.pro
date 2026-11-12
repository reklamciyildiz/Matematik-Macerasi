# Matematik Macerası - release R8 guardrails (2026-11)
# Narrow, reflection-specific rules. DO NOT add -dontobfuscate,
# -dontoptimize, -dontshrink, or blanket app/package -keep rules.
#
# R8 obfuscates remaining implementation; consumer rules from AndroidX,
# Compose, Play Billing, Firebase and Play Integrity are retained.

# Retrofit 2.9 / Gson 2.10.1 / Kotlin suspend endpoints need generic
# response types and runtime HTTP/JSON annotations at reflection time.
-keepattributes Signature,InnerClasses,EnclosingMethod,*Annotation*
-keep class kotlin.coroutines.Continuation

# Gson 2.10.1 predates the library's bundled R8 rules.
# TypeToken<T> uses a generic signature to recover the actual T.
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken

# JSON keys remain stable when fields are explicitly annotated, even after
# obfuscation. Never retain all Kotlin model classes or names as a workaround.
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Retrofit 2.9 uses dynamic proxies: annotated API methods are reflected.
# Keep only those annotated interface methods, not the entire library.
-keepclassmembers interface * {
    @retrofit2.http.* <methods>;
}

# Keep meaningful exception line numbers for retracing Play Console reports;
# obfuscation stays enabled and filenames can be normalized.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
