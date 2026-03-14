import SwiftUI
import FirebaseCore
import FirebaseAppCheck

class AppCheckProviderFactoryImpl: NSObject, AppCheckProviderFactory {
    func createProvider(with app: FirebaseApp) -> AppCheckProvider? {
        #if DEBUG
        return AppCheckDebugProvider(app: app)
        #else
        if #available(iOS 14.0, *) {
            return AppAttestProvider(app: app)
        } else {
            return DeviceCheckProvider(app: app)
        }
        #endif
    }
}

@main
struct iOSApp: App {
    init() {
        // App Check must be configured before FirebaseApp.configure()
        AppCheck.setAppCheckProviderFactory(AppCheckProviderFactoryImpl())
        FirebaseApp.configure()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
