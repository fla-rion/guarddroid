# GuardDroid ProGuard Rules

# Keep Device Admin Receiver
-keep class dev.guarddroid.app.receiver.GuardDroidAdminReceiver { *; }

# Keep Accessibility Service
-keep class dev.guarddroid.app.service.GuardDroidAccessibilityService { *; }

# Keep Hilt-generated classes
-keep class dagger.hilt.** { *; }
-keep @dagger.hilt.android.AndroidEntryPoint class * { *; }

# Keep Room entities
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }

# Keep ViewModel
-keep class * extends androidx.lifecycle.ViewModel { *; }

# Keep data classes
-keep class dev.guarddroid.core.device.DeviceInfo { *; }
-keep class dev.guarddroid.core.database.entity.** { *; }
-keep enum dev.guarddroid.core.common.** { *; }

# Security: Remove debug logs
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
}
