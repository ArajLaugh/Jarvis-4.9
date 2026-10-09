# JARVIS AI — Advanced Android Studio Project

## Open and build
1. Extract this ZIP.
2. Android Studio → Open → select the `jarvis` folder.
3. Use JDK 17 and install Android SDK Platform 35 if requested.
4. Wait for Gradle Sync, connect Android phone, enable USB debugging, and press Run.
5. APK: Build → Build APK(s). Debug APK is normally `app/build/outputs/apk/debug/app-debug.apk`.

## Existing code in this project
- Updated Jetpack Compose neon JARVIS dashboard inspired by `docs/JARVIS_Neon_Dashboard_Reference.png`
- Transcript card, animated green/cyan orb, Home/Chat/Memory/Soul/Tasks/Settings navigation, voice button, headline/task panels
- Microphone speech recognition flow
- AI provider abstraction and HTTP provider configuration panel
- Command planning / tool routing
- Text input, activity log, confirm/cancel UI and lockdown UI
- Basic app launch, timer, alarm, and dialer intents
- Secure storage helpers and action guard

## Requirements catalog (25 requested features)
This project includes the source code for the existing starter and the requirements catalog below. **It does not mean all 25 features are fully implemented or tested.** Android features that require roles, special access, an API provider, server support, or device testing must be completed and tested separately.

1. Full mobile control — partial; only supported system intents/tools work.
2. Lock-screen control — not implemented.
3. “Hey JARVIS” wake word — not implemented; requires a foreground microphone service or a supported wake-word engine.
4. Hindi voice assistant — partial; speech and TTS behavior depends on device language services.
5. Free AI API integration — provider abstraction exists; configure a compatible endpoint/model/token in Settings. Compatibility depends on provider schema.
6. Automatic call answering — not implemented; requires call role/permissions and device testing.
7. Outgoing calls — dialer intent exists; it opens the dialer and does not silently place a call.
8. Social app control — partial; can launch apps, but in-app automation is not implemented.
9. YouTube control — app launching may work; media/search automation is not implemented.
10. Voice app launching — partial; basic open-app tool exists.
11. Home/Back/Recent/Settings/Camera controls — not fully implemented; system-level actions need appropriate Android APIs/roles.
12. AI self-update — not implemented.
13. Automatic knowledge database updates — not implemented; needs a data source and update/sync layer.
14. Persistent conversation memory — not fully implemented; current log/session behavior is limited.
15. Automatic news updates — not implemented; requires a news source/API and refresh logic.
16. AI planning — basic provider-based planning abstraction exists; tool support is limited.
17. Multi-step task automation — not implemented as a reliable task engine.
18. Permission/safety system — basic action guard and confirm/cancel UI exist; audit all actions before production use.
19. Background service — not implemented.
20. Auto-start after reboot — not implemented.
21. Reference-inspired JARVIS HUD — updated native Compose dashboard is included; the reference image is in `docs/`.
22. Memory/Chat/Soul/Settings/Tasks sections — navigation UI is included; several sections remain visual placeholders.
23. Android Studio project and Gradle source — included.
24. Hindi setup instructions — included in this file.
25. APK build support — project can be built in Android Studio if Gradle sync/build succeeds.

## Banking and UPI
The previous name-based restriction on launching banking/UPI apps has been removed as requested. The app does not provide a special banking/UPI control layer. Android banking apps may still require their own authentication and may block automation.

## Security notes
- Never hard-code a personal API key in source code or ship it in a public APK; extracted APKs can expose it. A backend proxy is safer.
- Do not bypass Android lock screen, PIN, biometrics, or app protections.
- Call answering, always-on microphone, Accessibility control, and background operation require explicit permissions/roles and may be restricted by Android/OEM battery management.
- Confirm calls, messages, purchases, and other consequential actions.
