# Add project specific ProGuard rules here.
# Keep Firebase model classes
-keep class com.registro.alimentario.model.** { *; }
# Keep Hilt generated code
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
