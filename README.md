# FocusLock - Strict Android Focus & App Blocker

Created by Vidhya (package: `com.vidhya.focuslock`).

### Features
1. **Accessibility Service Interceptor**: Detects window state changes and displays `BlockActivity` over blocked packages.
2. **Foreground Timer Service**: Keeps the lock running reliably in the background without Android OS killing it.
3. **Anti-Reboot Protection**: `BootReceiver` checks for unexpired lock sessions and re-engages protection immediately after phone restarts.
4. **Device Admin Protection**: Configured with Device Admin policies so users cannot simply uninstall during an active session.
5. **Anti-Impulse Math Challenge**: Prevents impulsive dopamine checks by requiring solving complex 3-term arithmetic for emergency access.

### Built With
- **Android Gradle Plugin (AGP)**: 8.5.2
- **Kotlin**: 1.9.24
- **Compile & Target SDK**: 34 (Android 14)
- **Minimum SDK**: 26 (Android 8.0+)
