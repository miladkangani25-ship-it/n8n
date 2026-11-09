# Offline Persian/Russian Translator — Product Roadmap

## Product goal

Build a privacy-first application that works without network access and supports a complete two-way conversation loop:

1. Russian or Persian speech → text
2. Text normalization and language identification
3. Russian ↔ Persian translation
4. Translated text → Russian or Persian speech

The first release should optimize for reliable short utterances and conversational phrases on a modern Android phone, with a desktop reference runner for testing and model evaluation.

## User experience

### Conversation mode (MVP)

- Two large language buttons: فارسی and Русский.
- Push-to-talk or hold-to-talk recording; stop automatically after a configurable silence window.
- Show source transcript immediately, then translated text, then a play button for synthesized audio.
- Keep the last 20 turns on-device; allow copy, replay, edit, and delete.
- Display a clear offline indicator and never silently send audio or text to a network service.
- Let the user swap direction manually; provide an optional language detector as a convenience, with manual override always available.

### Accessibility and failure states

- Large controls, high-contrast text, right-to-left Persian layout support.
- Show “could not understand” separately from “translation uncertain.”
- If speech recognition confidence is low, allow transcript editing before translation.
- If a model is unavailable on the device, explain which model package is needed and provide an offline setup check.

## Technical data flow

```text
Microphone
  → audio capture (16 kHz mono PCM)
  → voice activity detection / segmentation
  → speech-to-text model (fa or ru)
  → normalization + language validation
  → translation model (fa↔ru)
  → text display
  → text-to-speech model (fa or ru)
  → audio playback
```

All pipeline stages should expose a typed local API so the UI can swap implementations and benchmark models without changing product code. Persist only user-approved transcripts and settings in a local database; keep temporary audio in memory or delete it after inference.

## Model strategy

### Speech to text

- Start with a Whisper-family model that has documented Persian and Russian support. Benchmark a small/medium quantized model for phone latency and word error rate.
- Keep a model adapter interface (`transcribe(audio, language, options)`) so a faster native runtime can replace the initial implementation.
- Add VAD before inference to reduce latency and battery use; retain timestamps and confidence where the runtime provides them.

### Translation

- Evaluate an offline multilingual sequence-to-sequence model with explicit Persian/Russian coverage. Compare a compact distilled model against a larger quality model on the same test set.
- Do not translate through English in the MVP unless direct fa↔ru quality is demonstrably worse; an English pivot increases latency and error compounding.
- Use language tags and direction-specific prompts/configuration. Keep terminology and user corrections local for future glossary support.

### Text to speech

- Use a local TTS runtime with voices for both Persian and Russian. Benchmark naturalness, startup latency, and package size separately for each voice.
- Cache synthesized audio for the current turn only; provide a speed control and avoid uploading text.

## MVP milestones

### M0 — Feasibility (1 week)

- Desktop CLI that runs the full pipeline on one recorded Persian clip and one Russian clip.
- Capture latency, peak memory, model sizes, transcript quality, translation quality, and generated audio availability.
- Decide model/runtime targets from evidence, not model popularity.

### M1 — Local pipeline (1–2 weeks)

- Typed pipeline API, model adapters, local fixtures, deterministic configuration, and command-line smoke test.
- Offline mode test that blocks network access and still completes inference.
- Golden test fixtures for short phrases in both directions.

### M2 — Android conversation UI (2–3 weeks)

- Push-to-talk recording, transcript/translation cards, playback, turn history, settings, and model installation status.
- Background inference with cancellation and progress feedback.
- RTL layout and accessibility pass.

### M3 — Quality and packaging (2 weeks)

- Quantized model packages, checksum verification, resumable local import, storage management, and crash-safe model switching.
- Benchmark on representative low/mid/high Android devices.
- Release candidate with a documented offline privacy audit.

## Evaluation plan

Create a versioned evaluation corpus with consented or public-licensed samples:

- At least 100 short utterances per language for STT, including colloquial speech, names, code-switching, and noisy recordings.
- At least 500 aligned Persian/Russian text pairs for translation, balanced by direction and topic.
- At least 50 phrases per TTS voice for pronunciation and prosody review.

Track:

- STT: word error rate and character error rate, separately for Persian and Russian.
- Translation: human adequacy/fluency ratings plus COMET/BLEU as secondary signals.
- TTS: intelligibility, naturalness, pronunciation error rate, first-audio latency, and real-time factor.
- Product: end-to-end success rate, p50/p95 latency, memory, battery impact, package size, and crash rate.

Keep a small “hard cases” set that is never used for model tuning. Run it on every model or preprocessing change.

## Privacy and security requirements

- Default to zero network permissions in inference builds where platform support permits it.
- Do not log raw audio, transcripts, or translations by default. Redact diagnostics and require explicit opt-in for sharing.
- Encrypt any persisted conversation history using platform storage APIs; provide “delete all data.”
- Verify model checksums before import and keep model files outside executable code paths.
- Document model licenses, training-data limitations, and redistribution obligations before shipping.

## Packaging targets

### Desktop development runner

- Python or TypeScript CLI with reproducible environment, local model directory, and JSONL input/output.
- Used for corpus evaluation, profiling, and regression tests; not the primary user product.

### Android application

- Native Kotlin shell or a carefully chosen cross-platform UI with native audio and inference bridges.
- Models downloaded/imported as versioned packages; inference must work in airplane mode after setup.
- Separate base APK from optional language-model packs where platform constraints allow.

### Future desktop app

- Reuse the pipeline API and model packages; add microphone device selection, transcript export, and accessibility features.

## Open decisions before implementation

- Android minimum API level and supported CPU architectures.
- Whether the first runtime is ONNX Runtime, llama.cpp-compatible, MediaPipe, or a vendor NNAPI delegate.
- Exact model licenses and whether commercial redistribution is allowed.
- Storage budget for both languages and whether users may install only one direction.
- Whether the initial product is a standalone app or a companion to the tested caption application.
