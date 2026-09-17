-keep class com.xtc.** { *; }
-keep class !corecloud.xtc.share.** { *; }

-dontwarn **
-ignorewarnings

-optimizationpasses 999
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*

-overloadaggressively
-allowaccessmodification
-repackageclasses ''
-flattenpackagehierarchy ''
-useuniqueclassmembernames

-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable
