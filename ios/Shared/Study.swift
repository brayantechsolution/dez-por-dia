import Foundation

/// O que está na tela agora: a palavra, o número dela no dia e se é a rodada no passado.
struct Card: Hashable {
    let word: Word
    let number: Int      // 1...10
    let pastRound: Bool  // depois das 10 palavras, elas voltam com a frase no passado
    let day: Int         // 1...130

    var sentence: String { pastRound ? (word.s2 ?? word.s) : word.s }
    var sentencePt: String { pastRound ? (word.t2 ?? word.t) : word.t }
    var chip: String { (pastRound ? "REVISÃO NO PASSADO · " : "PALAVRA ") + "\(number) DE \(Words.perDay)" }
    var shortChip: String { (pastRound ? "PASSADO " : "PALAVRA ") + "\(number)/\(Words.perDay)" }
}

/// Progresso do estudo, guardado num grupo compartilhado entre o app e os widgets.
enum Study {
    static let group = "group.app.dezpordia"
    static var store: UserDefaults { UserDefaults(suiteName: group) ?? .standard }

    private static let kStart = "start_day"
    private static let kWeekdays = "weekdays_only"
    private static let kOffset = "day_offset"
    private static let kStepsDay = "steps_day"
    private static let kSteps = "steps"

    /// A primeira palavra aparece às 7h e a próxima a cada 90 minutos (a 10ª às 20h30).
    static let firstHour = 7
    static let slotMinutes = 90

    static var calendar: Calendar { Calendar.current }

    static func ensureStart() {
        if store.object(forKey: kStart) == nil {
            store.set(calendar.startOfDay(for: Date()).timeIntervalSince1970, forKey: kStart)
        }
    }

    static var weekdaysOnly: Bool {
        get { store.object(forKey: kWeekdays) as? Bool ?? true }
        set { store.set(newValue, forKey: kWeekdays) }
    }

    static func dayKey(_ d: Date) -> Int {
        Int(calendar.startOfDay(for: d).timeIntervalSince1970 / 86_400)
    }

    /// Quantos dias de estudo já passaram antes de hoje (0 no primeiro dia).
    static func studyDayIndex(_ date: Date = Date()) -> Int {
        ensureStart()
        let start = calendar.startOfDay(for: Date(timeIntervalSince1970: store.double(forKey: kStart)))
        let today = calendar.startOfDay(for: date)
        var count = 0
        var d = start
        while d < today {
            if !weekdaysOnly || !calendar.isDateInWeekend(d) { count += 1 }
            guard let next = calendar.date(byAdding: .day, value: 1, to: d) else { break }
            d = next
        }
        return max(0, count + store.integer(forKey: kOffset))
    }

    /// Muda o dia de estudo de hoje (setas ◀ ▶) e recomeça da palavra 1.
    static func setStudyDay(_ target: Int, now: Date = Date()) {
        let t = max(0, min(Words.totalDays - 1, target))
        let current = studyDayIndex(now)
        store.set(store.integer(forKey: kOffset) + (t - current), forKey: kOffset)
        store.set(dayKey(now), forKey: kStepsDay)
        store.set(-slot(now), forKey: kSteps)
    }

    /// Em que horário do dia estamos: 0 até 8h29, 1 a partir de 8h30, e assim por diante.
    static func slot(_ d: Date) -> Int {
        let c = calendar.dateComponents([.hour, .minute], from: d)
        let minutes = (c.hour ?? 0) * 60 + (c.minute ?? 0) - firstHour * 60
        if minutes < 0 { return 0 }
        return min(Words.perDay - 1, minutes / slotMinutes)
    }

    /// Quantas vezes a pessoa tocou em "Próxima" hoje.
    static func steps(_ d: Date) -> Int {
        store.integer(forKey: kStepsDay) == dayKey(d) ? store.integer(forKey: kSteps) : 0
    }

    static func card(at d: Date = Date()) -> Card {
        let words = Words.forDay(studyDayIndex(d))
        let k = max(0, slot(d) + steps(d))
        let i = k % words.count
        return Card(word: words[i], number: i + 1,
                    pastRound: (k / words.count) % 2 == 1,
                    day: studyDayIndex(d) % Words.totalDays + 1)
    }

    static func next(now: Date = Date()) {
        let s = steps(now) + 1
        store.set(dayKey(now), forKey: kStepsDay)
        store.set(s, forKey: kSteps)
    }

    /// Horários em que a palavra muda sozinha a partir de agora (para a linha do tempo dos widgets).
    static func changeTimes(from now: Date) -> [Date] {
        let today = calendar.startOfDay(for: now)
        var out: [Date] = []
        for i in 1..<Words.perDay {
            if let t = calendar.date(byAdding: .minute, value: firstHour * 60 + i * slotMinutes, to: today), t > now {
                out.append(t)
            }
        }
        if let tomorrow = calendar.date(byAdding: .day, value: 1, to: today) { out.append(tomorrow) }
        return out
    }
}
