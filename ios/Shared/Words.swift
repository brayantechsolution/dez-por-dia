import Foundation

/// Uma palavra do banco (o mesmo words.json do app Android).
struct Word: Codable, Hashable {
    let w: String       // palavra em inglês
    let pt: String      // tradução
    let s: String       // frase
    let t: String       // tradução da frase
    let s2: String?     // frase no passado (rodada de revisão)
    let t2: String?
    let r: String?      // palavra de um dia anterior que aparece na frase
    let e: String?      // emoji
    let p: String?      // passado do verbo (só nos verbos)

    var past: String { p ?? "" }
    var emoji: String { e ?? "" }
    var review: String { r ?? "" }
}

enum Words {
    static let perDay = 10
    static let totalDays = 130

    static let all: [Word] = {
        guard let url = Bundle.main.url(forResource: "words", withExtension: "json"),
              let data = try? Data(contentsOf: url),
              let list = try? JSONDecoder().decode([Word].self, from: data),
              list.count >= perDay else {
            return [Word(w: "hello", pt: "olá", s: "Hello, how are you?", t: "Olá, como vai?",
                         s2: "I said hello to my neighbor.", t2: "Eu disse oi para o meu vizinho.",
                         r: nil, e: "👋", p: nil)]
        }
        return list
    }()

    /// As 10 palavras do dia de estudo `index` (0 = dia 1).
    static func forDay(_ index: Int) -> [Word] {
        let days = max(1, all.count / perDay)
        let d = ((index % days) + days) % days
        let start = d * perDay
        return Array(all[start..<min(start + perDay, all.count)])
    }
}
