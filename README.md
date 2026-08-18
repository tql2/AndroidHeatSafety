# AndroidHeatSafety

Android app for heat-safety information and heat-index guidance.

## Download the APK

Download the latest release APK from [`releases/AndroidHeatSafety.apk`](releases/AndroidHeatSafety.apk).

On an Android device, copy the APK to the device, open it, and allow installation from unknown sources if prompted.

For a connected device with Android Debug Bridge (ADB) enabled:

```powershell
adb install releases/AndroidHeatSafety.apk
```

## Recent fixes and improvements

- Updated the Android build from the legacy Gradle 7.4 / Android Gradle Plugin 7.3.1 toolchain to Gradle 9.5.0 / Android Gradle Plugin 9.3.0 with Java 17 and compile SDK 37.
- Removed the obsolete Google Play Services Analytics dependency, duplicate Material dependency, and unnecessary multidex dependency.
- Updated the ProGuard configuration and namespace/BuildConfig setup required by the modern Android Gradle Plugin.
- Fixed the emulator startup issue caused by the host Intel graphics crash by validating the app on a physical device and making the APK independently installable. BlueStacks can also be used as an emulator alternative; the Android SDK is still required to build the app.
- Refreshed the visual system with a teal safety palette, rounded controls, focused input states, improved spacing, elevated navigation, and card-style menu rows.

The app's heat-index calculations, navigation IDs, and existing content were preserved during the modernization.

## Build

Open this directory in Android Studio and allow Gradle to sync. The project uses the Gradle wrapper included in the repository and requires JDK 17 for the Android build.
