import ActivityKit
import Foundation

/// O cartão ao vivo da tela de bloqueio (Atividade ao Vivo).
struct WordActivityAttributes: ActivityAttributes {
    struct ContentState: Codable, Hashable {
        var en: String
        var pt: String
        var past: String
        var emoji: String
        var sentence: String
        var sentencePt: String
        var chip: String
        var day: Int
        var number: Int
    }
}

extension WordActivityAttributes.ContentState {
    init(_ c: Card) {
        self.init(en: c.word.w, pt: c.word.pt, past: c.word.past, emoji: c.word.emoji,
                  sentence: c.sentence, sentencePt: c.sentencePt, chip: c.chip,
                  day: c.day, number: c.number)
    }
}

/// Liga, atualiza e desliga o cartão. Só o app consegue fazer isso, não a extensão de widgets.
enum LiveCard {
    static var isRunning: Bool {
        #if WIDGET_EXTENSION
        return false
        #else
        return !Activity<WordActivityAttributes>.activities.isEmpty
        #endif
    }

    static var isAllowed: Bool {
        ActivityAuthorizationInfo().areActivitiesEnabled
    }

    static func start() async {
        #if !WIDGET_EXTENSION
        guard isAllowed else { return }
        for a in Activity<WordActivityAttributes>.activities {
            await a.end(nil, dismissalPolicy: .immediate)
        }
        let state = WordActivityAttributes.ContentState(Study.card())
        _ = try? Activity.request(attributes: WordActivityAttributes(),
                                  content: ActivityContent(state: state, staleDate: nil))
        #endif
    }

    static func update() async {
        #if !WIDGET_EXTENSION
        let state = WordActivityAttributes.ContentState(Study.card())
        for a in Activity<WordActivityAttributes>.activities {
            await a.update(ActivityContent(state: state, staleDate: nil))
        }
        #endif
    }

    static func end() async {
        #if !WIDGET_EXTENSION
        for a in Activity<WordActivityAttributes>.activities {
            await a.end(nil, dismissalPolicy: .immediate)
        }
        #endif
    }
}
