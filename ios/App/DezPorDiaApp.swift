import AppIntents
import SwiftUI

@main
struct DezPorDiaApp: App {
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}

/// Deixa "Começar as palavras do dia" disponível no app Atalhos e na Siri.
struct DezPorDiaShortcuts: AppShortcutsProvider {
    static var appShortcuts: [AppShortcut] {
        AppShortcut(
            intent: StartDayIntent(),
            phrases: [
                "Começar o \(.applicationName)",
                "Palavras do dia no \(.applicationName)"
            ],
            shortTitle: "Começar o dia",
            systemImageName: "text.book.closed"
        )
    }
}
