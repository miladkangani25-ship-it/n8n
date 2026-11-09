# Persian ↔ Russian offline route

The app now exposes Persian (`fa`) as a speech/caption language. Whisper Tiny/Base already contain `fa` in their multilingual language table. For Persian↔Russian text translation, use an installed multilingual Qwen model: `QwenModel.supportsLanguages` includes both `fa` and `ru`, so the route is direct and does not require an English bridge.

The OPUS-MT catalog remains English-targeted. No new third-party weights are bundled by this change. Download and license verification for Qwen remain governed by the existing manifests and notices.
