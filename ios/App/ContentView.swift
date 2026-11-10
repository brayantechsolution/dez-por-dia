import SwiftUI
import WidgetKit

struct ContentView: View {
    @Environment(\.scenePhase) private var phase
    @State private var card = Study.card()
    @State private var day = Study.studyDayIndex()
    @State private var weekdaysOnly = Study.weekdaysOnly
    @State private var liveOn = LiveCard.isRunning

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 20) {
                    dayRow
                    BigCard(card: card)
                    actions
                    lockSection
                    todayList
                    settings
                }
                .padding(16)
            }
            .background(Color.navy.ignoresSafeArea())
            .navigationTitle("10 por Dia")
        }
        .tint(Color.lime)
        .preferredColorScheme(.dark)
        .onChange(of: phase) { _, newPhase in
            if newPhase == .active { refresh() }
        }
    }

    // MARK: Partes da tela

    private var dayRow: some View {
        HStack {
            Button { goToDay(day - 1) } label: { Image(systemName: "chevron.left") }
                .disabled(day <= 0)
                .accessibilityLabel("Dia anterior")
            Spacer()
            Text("Dia \(day % Words.totalDays + 1) de \(Words.totalDays)")
                .font(.headline)
                .foregroundStyle(.white)
            Spacer()
            Button { goToDay(day + 1) } label: { Image(systemName: "chevron.right") }
                .disabled(day >= Words.totalDays - 1)
                .accessibilityLabel("Próximo dia")
        }
        .font(.title3.bold())
    }

    private var actions: some View {
        HStack(spacing: 10) {
            Button { next() } label: { Label("Próxima", systemImage: "arrow.right") }
                .buttonStyle(.borderedProminent)
                .foregroundStyle(Color.navy)
            Button { Task { await Speaker.shared.sayWord(card.word) } } label: {
                Label("Palavra", systemImage: "speaker.wave.2.fill")
            }
            .buttonStyle(.bordered)
            Button { Task { await Speaker.shared.saySentence(card.sentence) } } label: {
                Label("Frase", systemImage: "speaker.wave.2")
            }
            .buttonStyle(.bordered)
        }
        .font(.subheadline.bold())
    }

    private var lockSection: some View {
        Section {
            VStack(alignment: .leading, spacing: 12) {
                Button {
                    Task {
                        if liveOn { await LiveCard.end() } else { await LiveCard.start() }
                        refresh()
                    }
                } label: {
                    Label(liveOn ? "Tirar o cartão da tela de bloqueio" : "Mostrar o cartão na tela de bloqueio",
                          systemImage: liveOn ? "lock.open" : "lock.rectangle.on.rectangle")
                        .frame(maxWidth: .infinity)
                }
                .buttonStyle(.borderedProminent)
                .foregroundStyle(Color.navy)

                if !LiveCard.isAllowed {
                    Text("O cartão está desligado. Ligue em Ajustes › 10 por Dia › Atividades ao Vivo.")
                        .font(.footnote)
                        .foregroundStyle(Color.mist)
                }
                Tip(number: "1", text: "Widget na tela de bloqueio: toque e segure a tela de bloqueio › Personalizar › Bloqueio › Adicionar widgets › 10 por Dia. Ele troca a palavra sozinho a cada 90 minutos.")
                Tip(number: "2", text: "Cartão todo dia, sem abrir o app: no app Atalhos › Automação › Nova automação › Horário do dia (7h, diariamente) › Executar imediatamente › ação \"Começar o dia\" do 10 por Dia.")
                Tip(number: "3", text: "Widget na tela inicial, com Próxima e áudio: toque e segure a tela inicial › Editar › Adicionar widget › 10 por Dia.")
            }
        } header: {
            SectionTitle(text: "Na tela de bloqueio")
        }
    }

    private var todayList: some View {
        Section {
            VStack(spacing: 0) {
                ForEach(Array(Words.forDay(day).enumerated()), id: \.offset) { i, w in
                    HStack(spacing: 12) {
                        Text("\(i + 1)").font(.caption.bold()).foregroundStyle(Color.mist).frame(width: 20)
                        Text(w.emoji).font(.title3)
                        VStack(alignment: .leading, spacing: 2) {
                            HStack(spacing: 6) {
                                Text(w.w).font(.body.bold()).foregroundStyle(.white)
                                if !w.past.isEmpty {
                                    Text("· \(w.past)").font(.subheadline).foregroundStyle(Color.mist)
                                }
                            }
                            Text(w.pt).font(.subheadline).foregroundStyle(Color.mist)
                        }
                        Spacer()
                    }
                    .padding(.vertical, 8)
                    if i < Words.perDay - 1 { Divider().overlay(Color.navy2) }
                }
            }
        } header: {
            SectionTitle(text: "As 10 palavras de hoje")
        }
    }

    private var settings: some View {
        Section {
            Toggle("Só dias úteis (descansa no fim de semana)", isOn: $weekdaysOnly)
                .onChange(of: weekdaysOnly) { _, v in
                    Study.weekdaysOnly = v
                    reloadEverything()
                }
                .foregroundStyle(.white)
        } header: {
            SectionTitle(text: "Ajustes")
        }
    }

    // MARK: Ações

    private func refresh() {
        card = Study.card()
        day = Study.studyDayIndex()
        liveOn = LiveCard.isRunning
    }

    private func next() {
        Study.next()
        reloadEverything()
    }

    private func goToDay(_ target: Int) {
        Study.setStudyDay(target)
        reloadEverything()
    }

    private func reloadEverything() {
        WidgetCenter.shared.reloadAllTimelines()
        Task {
            await LiveCard.update()
            refresh()
        }
        refresh()
    }
}

/// O cartão grande do topo, igual ao da tela de bloqueio do Android.
struct BigCard: View {
    let card: Card

    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            HStack {
                Chip(text: card.chip)
                Spacer()
                Text(card.word.emoji).font(.system(size: 44))
            }
            WordWithPast(word: card.word.w, past: card.word.past, size: 40)
            Text(card.word.pt).font(.title3).foregroundStyle(Color.mist)
            Divider().overlay(Color.navy2)
            Text(card.sentence).font(.title3.bold()).foregroundStyle(.white)
            Text(card.sentencePt).font(.body).foregroundStyle(Color.mist)
            if !card.pastRound && !card.word.review.isEmpty {
                Text("Revisão: \(card.word.review)").font(.footnote).foregroundStyle(Color.lime)
            }
        }
        .padding(18)
        .background(RoundedRectangle(cornerRadius: 20).fill(Color.navy2))
    }
}

struct SectionTitle: View {
    let text: String

    var body: some View {
        Text(text.uppercased())
            .font(.caption.bold())
            .kerning(1)
            .foregroundStyle(Color.mist)
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(.bottom, 4)
    }
}

struct Tip: View {
    let number: String
    let text: String

    var body: some View {
        HStack(alignment: .top, spacing: 10) {
            Text(number)
                .font(.caption.bold())
                .foregroundStyle(Color.navy)
                .frame(width: 22, height: 22)
                .background(Circle().fill(Color.lime))
            Text(text).font(.footnote).foregroundStyle(.white)
        }
    }
}
