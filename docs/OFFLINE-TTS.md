# Offline Russian and Persian text-to-speech

The app now exposes `OfflineTextToSpeech`, a small adapter around Android's
`TextToSpeech` API. It accepts only voices whose `Voice.isNetworkConnectionRequired()`
flag is false, so synthesis cannot silently fall back to a cloud service.

The adapter supports `ru` (`ru-RU`) and `fa` (`fa-IR`) and reports explicit
failure reasons when the engine or offline voice pack is unavailable. Voice
packs are supplied by the selected Android TTS engine and are not bundled in
this repository; users must install them once in Android settings. Synthesis
runs locally after installation and does not require network access.

The current implementation is an Android system-voice integration rather than
a bundled neural TTS model. This keeps the APK small and uses the device's
optimized voice engines. A future bundled backend can implement the same
contract after model licensing, APK size, memory, and CPU/NPU performance have
been evaluated. Do not ship a model without recording its license and voice
dataset terms in `app/src/main/assets/licenses/`.

## Integration contract

1. Construct `OfflineTextToSpeech` with an application `Context` and callback.
2. Call `selectLanguage("fa")` or `selectLanguage("ru")` after initialization.
3. Enable the play button only after the callback reports `ready`.
4. Call `speak(text, uniqueUtteranceId)` and handle callback completion/errors.
5. Call `close()` with the owning screen/service lifecycle.

The adapter never requests Internet permission and rejects unavailable/offline
voices instead of changing to an online voice.
