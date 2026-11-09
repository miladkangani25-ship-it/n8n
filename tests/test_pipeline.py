from offline_translator.pipeline import OfflinePipeline

class STT:
    def transcribe(self, audio, language): return "سلام"
class Translator:
    def translate(self, text, source, target): return "Привет"
class TTS:
    def synthesize(self, text, language): return b"wav"

def test_pipeline_connects_all_stages():
    result = OfflinePipeline(STT(), Translator(), TTS()).process(b"pcm", "fa", "ru")
    assert result.source_text == "سلام"
    assert result.translated_text == "Привет"
    assert result.audio == b"wav"
