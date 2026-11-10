import SwiftUI

extension Color {
    static let navy = Color(red: 0x0F / 255, green: 0x1B / 255, blue: 0x2D / 255)
    static let navy2 = Color(red: 0x18 / 255, green: 0x28 / 255, blue: 0x3E / 255)
    static let lime = Color(red: 0xC6 / 255, green: 0xF4 / 255, blue: 0x32 / 255)
    static let mist = Color(red: 0xA9 / 255, green: 0xB4 / 255, blue: 0xC4 / 255)
}

/// Selo verde-limão "PALAVRA 2 DE 10".
struct Chip: View {
    let text: String

    var body: some View {
        Text(text)
            .font(.system(size: 10, weight: .bold))
            .kerning(0.8)
            .foregroundStyle(Color.navy)
            .padding(.horizontal, 8)
            .padding(.vertical, 4)
            .background(Capsule().fill(Color.lime))
            .lineLimit(1)
    }
}

/// A palavra grande com o passado ao lado, quando é verbo (ask   PASSADO asked).
struct WordWithPast: View {
    let word: String
    let past: String
    var size: CGFloat = 30

    var body: some View {
        HStack(alignment: .lastTextBaseline, spacing: 10) {
            Text(word)
                .font(.system(size: size, weight: .bold))
                .foregroundStyle(Color.lime)
                .lineLimit(1)
                .minimumScaleFactor(0.6)
            if !past.isEmpty {
                VStack(alignment: .leading, spacing: 0) {
                    Text("PASSADO")
                        .font(.system(size: 9, weight: .bold))
                        .kerning(0.8)
                        .foregroundStyle(Color.mist)
                    Text(past)
                        .font(.system(size: size * 0.55, weight: .bold))
                        .foregroundStyle(.white)
                        .lineLimit(1)
                        .minimumScaleFactor(0.6)
                }
                .accessibilityLabel("passado: \(past)")
            }
        }
    }
}
