# Alexa–ChatGPT Controller — Phase 1

Diagnostic Android application. It publishes a virtual media session, plays no audio, and logs Android MediaSession callbacks. It does not yet launch or control ChatGPT.

Target: Lenovo TB-8505X / Android 10. Kotlin, minSdk 23, targetSdk 29.

## Test
1. Install the APK from the GitHub Actions artifact.
2. Pair/connect the Echo and Lenovo as in the existing working Bluetooth setup.
3. Open this app and tap Start virtual media player.
4. Say "Alexa, play", then "Alexa, stop".
5. Inspect the log and note the received PLAY/PAUSE/STOP callbacks.

Bluetooth routing depends on Android, the Echo, and active media-session selection. The callback log is the definitive test.
