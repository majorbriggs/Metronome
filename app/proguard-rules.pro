# Preserve source file names and line numbers for readable crash reports.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Kotlin metadata is required for reflection used by coroutines and Hilt.
-keep class kotlin.Metadata { *; }
-keepclassmembernames class kotlinx.** { volatile <fields>; }

# Hilt-generated component glue code.
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.internal.GeneratedComponent { *; }
-keepclassmembers,allowobfuscation class * {
    @javax.inject.Inject <init>(...);
    @javax.inject.Inject <fields>;
}

# Preserve our enums so DataStore serialisation doesn't break.
-keepclassmembers enum com.majorbriggs.metronome.** { *; }
