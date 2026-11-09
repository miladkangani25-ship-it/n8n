package com.localcaption.instant;

import android.content.Context;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;
import java.util.Locale;
import java.util.Set;

/**
 * Offline-only Android TTS adapter for the conversation translator.
 *
 * Android supplies the engine and voice pack. A voice is accepted only when
 * the engine reports that it does not require a network connection.
 */
public final class OfflineTextToSpeech implements AutoCloseable {
    public interface Callback {
        void onReady(boolean ready, String reason);
        void onStart(String utteranceId);
        void onDone(String utteranceId);
        void onError(String utteranceId, String reason);
    }

    private final TextToSpeech engine;
    private final Callback callback;
    private volatile boolean ready;
    private volatile Locale locale;

    public OfflineTextToSpeech(Context context, Callback callback) {
        this.callback = callback;
        engine = new TextToSpeech(context.getApplicationContext(), status -> {
            if (status != TextToSpeech.SUCCESS) {
                ready = false;
                callback.onReady(false, "engine-init-" + status);
                return;
            }
            engine.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override public void onStart(String id) { callback.onStart(id); }
                @Override public void onDone(String id) { callback.onDone(id); }
                @Override public void onError(String id) { callback.onError(id, "synthesis-error"); }
                @Override public void onError(String id, int errorCode) { callback.onError(id, "synthesis-error-" + errorCode); }
            });
            callback.onReady(false, "select-language");
        });
    }

    /** Selects a local voice for Persian (fa) or Russian (ru). */
    public synchronized boolean selectLanguage(String languageTag) {
        Locale requested = localeFor(languageTag);
        if (requested == null) {
            ready = false;
            callback.onReady(false, "unsupported-language");
            return false;
        }
        int result = engine.setLanguage(requested);
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            ready = false;
            callback.onReady(false, "voice-pack-unavailable");
            return false;
        }
        Voice voice = offlineVoice(engine.getVoices(), requested);
        if (voice == null) {
            ready = false;
            callback.onReady(false, "offline-voice-unavailable");
            return false;
        }
        engine.setVoice(voice);
        locale = requested;
        ready = true;
        callback.onReady(true, "ready");
        return true;
    }

    public boolean isReady() { return ready; }

    /** Returns true only for the two offline conversation languages. */
    public static boolean supportsLanguage(String languageTag) { return localeFor(languageTag) != null; }
    public Locale selectedLocale() { return locale; }

    /** Queues text only after an offline voice has been selected. */
    public synchronized boolean speak(String text, String utteranceId) {
        if (utteranceId == null || utteranceId.trim().isEmpty()) {
            callback.onError(utteranceId, "utterance-id-required");
            return false;
        }
        if (!ready || text == null || text.trim().isEmpty()) {
            callback.onError(utteranceId, "offline-voice-not-ready");
            return false;
        }
        Bundle params = new Bundle();
        int result = engine.speak(text.trim(), TextToSpeech.QUEUE_FLUSH, params, utteranceId);
        if (result != TextToSpeech.SUCCESS) callback.onError(utteranceId, "speak-rejected-" + result);
        return result == TextToSpeech.SUCCESS;
    }

    public synchronized void stop() { engine.stop(); }

    @Override public synchronized void close() {
        ready = false;
        engine.stop();
        engine.shutdown();
    }

    static Locale localeFor(String languageTag) {
        if (languageTag == null) return null;
        String normalized = languageTag.toLowerCase(Locale.ROOT).replace('_', '-');
        if (normalized.equals("fa") || normalized.startsWith("fa-")) return Locale.forLanguageTag("fa-IR");
        if (normalized.equals("ru") || normalized.startsWith("ru-")) return Locale.forLanguageTag("ru-RU");
        return null;
    }

    static Voice offlineVoice(Set<Voice> voices, Locale requested) {
        if (voices == null) return null;
        Voice fallback = null;
        for (Voice voice : voices) {
            Locale voiceLocale = voice.getLocale();
            if (!requested.getLanguage().equalsIgnoreCase(voiceLocale.getLanguage())) continue;
            if (voice.isNetworkConnectionRequired()) continue;
            if (requested.equals(voiceLocale)) return voice;
            if (fallback == null) fallback = voice;
        }
        return fallback;
    }
}
