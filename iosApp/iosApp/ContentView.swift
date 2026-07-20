import SwiftUI
import shared

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        ScreenHosts_iosKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    @AppStorage("useLiquidGlassNavigation") private var useLiquidGlassNavigation = true

    var body: some View {
        Group {
            if #available(iOS 26.1, *), useLiquidGlassNavigation {
                NativeNavContentView()
                    .onOpenURL(perform: handleDeepLink)
            } else {
                ComposeView()
                    .ignoresSafeArea(.all)
                    .onOpenURL(perform: handleDeepLink)
            }
        }
        .preferredColorScheme(.dark)
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
