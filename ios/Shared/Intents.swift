import AppIntents
import WidgetKit

/// Botão "Próxima" (widget, cartão ao vivo e Atalhos).
struct NextWordIntent: LiveActivityIntent {
    static let title: LocalizedStringResource = "Próxima palavra"
    static let description = IntentDescription("Mostra a próxima palavra do dia.")

    func perform() async throws -> some IntentResult {
        Study.next()
        WidgetCenter.shared.reloadAllTimelines()
        await LiveCard.update()
        return .result()
    }
}

/// Botão "🔊": fala a palavra (e o passado, se for verbo).
struct SpeakWordIntent: AudioPlaybackIntent {
    static let title: LocalizedStringResource = "Ouvir a palavra"

    func perform() async throws -> some IntentResult {
        await Speaker.shared.sayWord(Study.card().word)
        return .result()
    }
}

/// Botão "🔊 Frase": lê a frase que está na tela.
struct SpeakSentenceIntent: AudioPlaybackIntent {
    static let title: LocalizedStringResource = "Ouvir a frase"

    func perform() async throws -> some IntentResult {
        await Speaker.shared.saySentence(Study.card().sentence)
        return .result()
    }
}

/// Liga o cartão ao vivo na tela de bloqueio. Feito para uma automação do Atalhos às 7h.
struct StartDayIntent: LiveActivityIntent {
    static let title: LocalizedStringResource = "Começar as palavras do dia"
    static let description = IntentDescription("Coloca a palavra do dia na tela de bloqueio.")

    func perform() async throws -> some IntentResult {
        await LiveCard.start()
        WidgetCenter.shared.reloadAllTimelines()
        return .result()
    }
}
