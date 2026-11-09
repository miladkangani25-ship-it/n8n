package com.localcaption.instant;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/** Fast contract checks that do not require an installed voice pack. */
@RunWith(AndroidJUnit4.class)
public final class OfflineTextToSpeechVerification {
    @Test public void languageContractIsOfflineAndBounded() {
        assertTrue(OfflineTextToSpeech.supportsLanguage("fa"));
        assertTrue(OfflineTextToSpeech.supportsLanguage("fa-IR"));
        assertTrue(OfflineTextToSpeech.supportsLanguage("ru-RU"));
        assertFalse(OfflineTextToSpeech.supportsLanguage("en"));
        assertFalse(OfflineTextToSpeech.supportsLanguage("en-US"));
        assertFalse(OfflineTextToSpeech.supportsLanguage(null));
        assertEquals("fa", OfflineTextToSpeech.localeFor("fa-IR").getLanguage());
        assertEquals("ru", OfflineTextToSpeech.localeFor("ru").getLanguage());
    }
}
