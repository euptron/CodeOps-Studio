# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Use obfuscation dictionaries to rename classes, packages, and methods
-obfuscationdictionary dictionary.txt
-packageobfuscationdictionary dictionary.txt
-classobfuscationdictionary dictionary.txt

### Crash report
# Preserve the line number information for debugging stack traces and hide 
# original source file name for security.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

### Build process
# Ignore all warnings during the build process.
#-ignorewarnings
# Suppress warnings for all classes.
#-dontwarn **
# Suppress informational notes for all classes.
#-dontnote **

# Keep all classes and their members in the com.eup.codeopsstudio package and subpackages,
# allow obfuscation and optimization, but do not remove them.
-keep,allowobfuscation class com.eup.codeopsstudio.** { *; }

# Eclipse
-keep class org.eclipse.** { *; }

# Keep the missing classes
-keep class java.beans.** { *; }
-keep class org.ietf.jgss.** { *; }
-keep class org.slf4j.impl.StaticLoggerBinder { *; }

# Ignore warnings for the missing classes
-dontwarn java.beans.**
-dontwarn org.ietf.jgss.**
-dontwarn org.slf4j.impl.**

# Gson
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

## Retain generic signatures of TypeToken and its subclasses with R8 version 3.0 and higher.
-keep,allowobfuscation,allowshrinking class com.google.gson.reflect.TypeToken
-keep,allowobfuscation,allowshrinking class * extends com.google.gson.reflect.TypeToken

# Keep all AdMob SDK classes to ensure they work correctly
-keep class com.google.android.gms.ads.** { *; }
-keep class com.google.ads.** { *; }

# Keep AndroidX Preferences and Lifecycle classes to ensure they work correctly
-keep class androidx.preference.** { *; }
-keep class androidx.lifecycle.** { *; }
-keep class * extends androidx.fragment.app.Fragment { public *; }

# Keep constructors for LiveData and ViewModel classes
-keepclassmembers class androidx.lifecycle.LiveData {
    <init>(...);
}
-keepclassmembers class androidx.lifecycle.MutableLiveData {
    <init>(...);
}
-keepclassmembers class androidx.lifecycle.ViewModel {
    <init>(...);
}

# Keep all members of DefaultLifecycleObserver to ensure lifecycle methods are not obfuscated
-keep class androidx.lifecycle.DefaultLifecycleObserver {
    *;
}

# Keep all Parcelable implementations and their CREATOR fields
-keep public class * implements android.os.Parcelable
-keepclassmembers public class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Keep specific methods related to Serializable implementation
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Keep methods for enum classes
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# If you're using custom Exception 
-keep public class * extends java.lang.Exception
-keep class com.crashlytics.** { *; }
-dontwarn com.crashlytics.**
-keep class com.google.firebase.crashlytics.** { *; }
-dontwarn com.google.firebase.crashlytics.**

# Pane
-keep class * extends com.eup.codeopsstudio.pane.Pane { public *; }

# Firebase crashlytics
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable

-keepclassmembers class * {
    @com.google.firebase.crashlytics.* <fields>;
}

-keepnames class com.google.firebase.crashlytics.** { *; }
-dontwarn com.google.firebase.crashlytics.**
