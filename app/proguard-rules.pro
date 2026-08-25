# Kotlinx serialization
-keepattributes *Annotation*, InnerClasses, Signature, RuntimeVisible*Annotations, AnnotationDefault
-dontnote kotlinx.serialization.**
-keepclassmembers class com.catokids.app.** {
    *** Companion;
    <fields>;
}
-keepclasseswithmembers class com.catokids.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.catokids.app.**$$serializer { *; }

# Ktor
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**
-dontwarn org.slf4j.**
