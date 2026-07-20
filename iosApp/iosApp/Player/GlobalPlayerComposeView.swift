import SwiftUI
import shared

@available(iOS 17.0, *)
struct GlobalPlayerComposeView: UIViewControllerRepresentable {
    let initialExpanded: Bool
    let onNavigate: (Route) -> Void
    let onHeightChanged: (Bool) -> Void
    let onVisibilityChanged: (Bool) -> Void

    func makeUIViewController(context: Context) -> UIViewController {
        let controller = IosPlayerViewControllerKt.PlayerViewController(
            initialExpanded: initialExpanded,
            onNavigate: { route in
                DispatchQueue.main.async {
                    onNavigate(route)
                }
            },
            onHeightChanged: { expanded in
                DispatchQueue.main.async {
                    onHeightChanged(expanded.boolValue)
                }
            },
            onVisibilityChanged: { visible in
                DispatchQueue.main.async {
                    onVisibilityChanged(visible.boolValue)
                }
            }
        )
        controller.view.backgroundColor = .clear
        return controller
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
