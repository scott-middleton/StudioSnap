import SwiftUI
import FirebaseCore
import ComposeApp

@main
struct iOSApp: App {
    init() {
        FirebaseApp.configure()
        // Initialize Koin and RevenueCat BEFORE Compose renders
        InitKoinKt.doInitKoin()
        PurchasesManager.shared.configure()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
