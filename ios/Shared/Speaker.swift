import AVFoundation

/// Fala a palavra e a frase em inglês com a voz do próprio iPhone (funciona sem internet).
final class Speaker: NSObject, AVSpeechSynthesizerDelegate {
    static let shared = Speaker()

    private let synth = AVSpeechSynthesizer()
    private var finished: CheckedContinuation<Void, Never>?

    override init() {
        super.init()
        synth.delegate = self
    }

    /// A palavra no ritmo normal, depois devagar e, se for verbo, o passado: "ask… ask… asked".
    func sayWord(_ word: Word) async {
        var parts: [(String, Float)] = [(word.w, 0.85), (word.w, 0.6)]
        if !word.past.isEmpty {
            parts.append((word.past.replacingOccurrences(of: "was/were", with: "was"), 0.6))
        }
        await say(parts)
    }

    func saySentence(_ text: String) async {
        await say([(text, 0.85)])
    }

    private func say(_ parts: [(String, Float)]) async {
        let session = AVAudioSession.sharedInstance()
        try? session.setCategory(.playback, mode: .spokenAudio, options: [.duckOthers])
        try? session.setActive(true)
        synth.stopSpeaking(at: .immediate)
        for (i, part) in parts.enumerated() {
            let u = AVSpeechUtterance(string: part.0)
            u.voice = AVSpeechSynthesisVoice(language: "en-US")
            u.rate = AVSpeechUtteranceDefaultSpeechRate * part.1
            u.postUtteranceDelay = i < parts.count - 1 ? 0.5 : 0
            await withCheckedContinuation { (c: CheckedContinuation<Void, Never>) in
                finished = c
                synth.speak(u)
            }
        }
        try? session.setActive(false, options: .notifyOthersOnDeactivation)
    }

    func speechSynthesizer(_ synthesizer: AVSpeechSynthesizer, didFinish utterance: AVSpeechUtterance) {
        resume()
    }

    func speechSynthesizer(_ synthesizer: AVSpeechSynthesizer, didCancel utterance: AVSpeechUtterance) {
        resume()
    }

    private func resume() {
        let c = finished
        finished = nil
        c?.resume()
    }
}
