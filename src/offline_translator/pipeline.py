from dataclasses import dataclass
from typing import Protocol, Literal

Language = Literal["fa", "ru"]

class SpeechToText(Protocol):
    def transcribe(self, audio: bytes, language: Language) -> str: ...

class Translator(Protocol):
    def translate(self, text: str, source: Language, target: Language) -> str: ...

class TextToSpeech(Protocol):
    def synthesize(self, text: str, language: Language) -> bytes: ...

@dataclass
class TranslationResult:
    source_text: str
    translated_text: str
    audio: bytes

class OfflinePipeline:
    def __init__(self, stt: SpeechToText, translator: Translator, tts: TextToSpeech):
        self.stt, self.translator, self.tts = stt, translator, tts

    def process(self, audio: bytes, source: Language, target: Language) -> TranslationResult:
        text = self.stt.transcribe(audio, source)
        translated = self.translator.translate(text, source, target)
        speech = self.tts.synthesize(translated, target)
        return TranslationResult(text, translated, speech)
