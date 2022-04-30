#-keep class com.example.justlisten.datalayer.models.*
#-keep class com.example.justlisten.datalayer.webservices.apis.playlistcalls.*

# Keep `INSTANCE.serializer()` of serializable objects.
#-if @kotlinx.serialization.Serializable class ** {
#    public static ** INSTANCE;
#}
#-keepclassmembers class <1> {
#    public static <1> INSTANCE;
#    kotlinx.serialization.KSerializer serializer(...);
#}


-keepattributes Annotation, InnerClasses
-dontnote kotlinx.serialization.SerializationKt
-keep,includedescriptorclasses class com.example.justlisten.datalayer.$$serializer {  } # <-- change package name to your app's
-keepclassmembers class com.example.justlisten.datalayer. { # <-- change package name to your app's
*** Companion;
}
-keepclasseswithmembers class com.example.justlisten.datalayer.* { # <-- change package name to your app's
kotlinx.serialization.KSerializer serializer(...);
}

