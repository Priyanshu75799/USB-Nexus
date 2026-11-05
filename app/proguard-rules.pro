# ProGuard / R8 optimization rules for USB Nexus

# Keep Domain Data Models and Entities
-keep class com.priyanshudeveloper.usbnexus.phoenixengine.domain.model.** { *; }
-keep class com.priyanshudeveloper.usbnexus.phoenixengine.data.** { *; }

# Retain source files and line numbers for stack traces
-keepattributes SourceFile,LineNumberTable
