import AppIntents
import SwiftUI
import WidgetKit

@main
struct DezPorDiaWidgets: WidgetBundle {
    var body: some Widget {
        LockWidget()
        HomeWidget()
        WordLiveActivity()
    }
}

// MARK: - Linha do tempo: a palavra muda sozinha a cada 90 minutos

struct WordEntry: TimelineEntry {
    let date: Date
    let card: Card
}

struct WordProvider: TimelineProvider {
    func placeholder(in context: Context) -> WordEntry {
        WordEntry(date: Date(), card: Study.card())
    }

    func getSnapshot(in context: Context, completion: @escaping (WordEntry) -> Void) {
        completion(WordEntry(date: Date(), card: Study.card()))
    }

    func getTimeline(in context: Context, completion: @escaping (Timeline<WordEntry>) -> Void) {
        let now = Date()
        var entries = [WordEntry(date: now, card: Study.card(at: now))]
        for t in Study.changeTimes(from: now) {
            entries.append(WordEntry(date: t, card: Study.card(at: t)))
        }
        completion(Timeline(entries: entries, policy: .atEnd))
    }
}

// MARK: - Tela de bloqueio

struct LockWidget: Widget {
    var body: some WidgetConfiguration {
        StaticConfiguration(kind: "lock", provider: WordProvider()) { entry in
            LockWidgetView(entry: entry)
        }
        .configurationDisplayName("Palavra na tela de bloqueio")
        .description("A palavra do dia, que troca sozinha a cada 90 minutos.")
        .supportedFamilies([.accessoryRectangular, .accessoryInline])
    }
}

struct LockWidgetView: View {
    @Environment(\.widgetFamily) private var family
    let entry: WordEntry

    var body: some View {
        let c = entry.card
        Group {
            if family == .accessoryInline {
                Text("\(c.word.emoji) \(c.word.w) — \(c.word.pt)")
            } else {
                VStack(alignment: .leading, spacing: 1) {
                    HStack(spacing: 4) {
                        Text(c.word.w).font(.headline).widgetAccentable()
                        if !c.word.past.isEmpty {
                            Text("· \(c.word.past)").font(.caption).opacity(0.8)
                        }
                    }
                    .lineLimit(1)
                    Text(c.word.pt).font(.caption).lineLimit(1)
                    Text(c.sentence).font(.caption2).lineLimit(1).opacity(0.85)
                }
                .frame(maxWidth: .infinity, alignment: .leading)
            }
        }
        .containerBackground(.clear, for: .widget)
    }
}

// MARK: - Tela inicial, com "Próxima" e áudio

struct HomeWidget: Widget {
    var body: some WidgetConfiguration {
        StaticConfiguration(kind: "home", provider: WordProvider()) { entry in
            HomeWidgetView(entry: entry)
        }
        .configurationDisplayName("10 por Dia")
        .description("A palavra do dia com os botões Próxima e ouvir.")
        .supportedFamilies([.systemSmall, .systemMedium])
    }
}

struct HomeWidgetView: View {
    @Environment(\.widgetFamily) private var family
    let entry: WordEntry

    var body: some View {
        let c = entry.card
        let small = family == .systemSmall
        VStack(alignment: .leading, spacing: 5) {
            HStack {
                Chip(text: small ? c.shortChip : c.chip)
                Spacer(minLength: 4)
                Text(c.word.emoji).font(.system(size: small ? 20 : 26))
            }
            WordWithPast(word: c.word.w, past: c.word.past, size: small ? 22 : 28)
            Text(c.word.pt).font(.caption).foregroundStyle(Color.mist).lineLimit(1)
            if !small {
                Text(c.sentence)
                    .font(.system(size: 13, weight: .semibold))
                    .foregroundStyle(.white)
                    .lineLimit(2)
            }
            Spacer(minLength: 0)
            HStack(spacing: 6) {
                Button(intent: NextWordIntent()) {
                    Label("Próxima", systemImage: "arrow.right")
                }
                Button(intent: SpeakWordIntent()) {
                    Image(systemName: "speaker.wave.2.fill")
                        .accessibilityLabel("Ouvir a palavra")
                }
                if !small {
                    Button(intent: SpeakSentenceIntent()) {
                        Label("Frase", systemImage: "speaker.wave.2")
                    }
                }
            }
            .font(.system(size: 12, weight: .bold))
            .tint(Color.lime)
        }
        .containerBackground(Color.navy, for: .widget)
    }
}

// MARK: - Cartão ao vivo na tela de bloqueio

struct WordLiveActivity: Widget {
    var body: some WidgetConfiguration {
        ActivityConfiguration(for: WordActivityAttributes.self) { context in
            LiveCardView(state: context.state)
                .activityBackgroundTint(Color.navy)
                .activitySystemActionForegroundColor(Color.lime)
        } dynamicIsland: { context in
            DynamicIsland {
                DynamicIslandExpandedRegion(.leading) {
                    Text(context.state.emoji).font(.title)
                }
                DynamicIslandExpandedRegion(.center) {
                    VStack(spacing: 2) {
                        Text(context.state.en).font(.title3.bold()).foregroundStyle(Color.lime)
                        Text(context.state.pt).font(.caption).foregroundStyle(Color.mist)
                    }
                }
                DynamicIslandExpandedRegion(.trailing) {
                    Button(intent: NextWordIntent()) {
                        Image(systemName: "arrow.right")
                            .accessibilityLabel("Próxima palavra")
                    }
                    .tint(Color.lime)
                }
                DynamicIslandExpandedRegion(.bottom) {
                    Text(context.state.sentence).font(.caption).lineLimit(2)
                }
            } compactLeading: {
                Text(context.state.emoji)
            } compactTrailing: {
                Text(context.state.en).font(.caption.bold()).foregroundStyle(Color.lime).lineLimit(1)
            } minimal: {
                Text(context.state.emoji)
            }
        }
    }
}

struct LiveCardView: View {
    let state: WordActivityAttributes.ContentState

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            HStack {
                Chip(text: state.chip)
                Spacer()
                Text("Dia \(state.day)").font(.caption2).foregroundStyle(Color.mist)
            }
            HStack(alignment: .center, spacing: 10) {
                Text(state.emoji).font(.system(size: 30))
                VStack(alignment: .leading, spacing: 1) {
                    WordWithPast(word: state.en, past: state.past, size: 24)
                    Text(state.pt).font(.caption).foregroundStyle(Color.mist).lineLimit(1)
                }
                Spacer(minLength: 4)
                Button(intent: NextWordIntent()) {
                    Image(systemName: "arrow.right")
                        .accessibilityLabel("Próxima palavra")
                }
                Button(intent: SpeakWordIntent()) {
                    Image(systemName: "speaker.wave.2.fill")
                        .accessibilityLabel("Ouvir a palavra")
                }
            }
            .tint(Color.lime)
            Text(state.sentence)
                .font(.system(size: 13, weight: .semibold))
                .foregroundStyle(.white)
                .lineLimit(2)
        }
        .padding(12)
    }
}
