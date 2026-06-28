import SwiftUI
import shared

@main
struct iOSApp: App {
    init() {
        IOSModuleKt.doInitKoin(apiKey: "")
    }
    
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}