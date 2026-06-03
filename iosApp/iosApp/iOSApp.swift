import SwiftUI
import shared

@main
struct iOSApp: App {
    init() {
        let apiKey = Bundle.main.object(forInfoDictionaryKey: "AUDIUS_API_KEY") as? String ?? ""
        IOSModuleKt.doInitKoin(apiKey: apiKey)
    }
    
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}