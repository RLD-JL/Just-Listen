import SwiftUI
import shared

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        return ScreenHosts_iosKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        ComposeView()
            .ignoresSafeArea(.all)
            .onOpenURL { url in
                handleDeepLink(url)
            }
    }
    
    private func handleDeepLink(_ url: URL) {
        guard url.scheme == "justlisten" else { return }
        
        if url.host == "oauth" {
            if let components = URLComponents(url: url, resolvingAgainstBaseURL: true),
               let queryItems = components.queryItems,
               let code = queryItems.first(where: { $0.name == "code" })?.value {
                let redirectUri = "justlisten://oauth/callback"
                IOSModuleKt.loginWithCode(code: code, redirectUri: redirectUri)
            }
        } else {
            IOSModuleKt.handleDeepLink(url: url.absoluteString)
        }
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}