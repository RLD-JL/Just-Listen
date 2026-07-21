import SwiftUI
import shared
import Network

@main
struct iOSApp: App {
    private let networkMonitor = NWPathMonitor()

    init() {
        IOSModuleKt.doInitKoin(apiKey: "")
        networkMonitor.pathUpdateHandler = { path in
            if path.status == .satisfied {
                DispatchQueue.main.async {
                    IOSModuleKt.retrySessionRestoration()
                }
            }
        }
        networkMonitor.start(queue: DispatchQueue(label: "JustListen.NetworkRecovery"))
    }
    
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
