# Project-specific R8/ProGuard rules.
# Keep source file and line number metadata so release crash traces remain useful.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep classes referenced directly from the Android manifest.
-keep class com.nekolaska.hificalculator.MainActivity { *; }
