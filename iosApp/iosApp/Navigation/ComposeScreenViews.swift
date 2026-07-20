import SwiftUI
import shared

private var activeKeyWindowSafeAreaBottom: CGFloat {
    UIApplication.shared.connectedScenes
        .filter { $0.activationState == .foregroundActive }
        .compactMap { $0 as? UIWindowScene }
        .first?.windows
        .first { $0.isKeyWindow }?
        .safeAreaInsets.bottom ?? 0
}

@available(iOS 17.0, *)
struct DetailComposeView: UIViewControllerRepresentable {
    let routeID: UUID
    let route: Route
    let coordinator: TabNavigationCoordinator

    func makeUIViewController(context: Context) -> UIViewController {
        coordinator.controllerStore.detail(id: routeID) {
            ScreenHosts_iosKt.ScreenViewController(
                route: route,
                bottomSafeArea: Double(activeKeyWindowSafeAreaBottom),
                onNavigate: { newRoute in
                    DispatchQueue.main.async {
                        coordinator.push(newRoute)
                    }
                },
                onPopBackStack: {
                    DispatchQueue.main.async {
                        coordinator.pop()
                    }
                }
            )
        }
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

@available(iOS 17.0, *)
struct NativeNavComposeView: UIViewControllerRepresentable {
    let topLevelRoute: Route
    let coordinator: TabNavigationCoordinator

    func makeUIViewController(context: Context) -> UIViewController {
        coordinator.controllerStore.root {
            ScreenHosts_iosKt.ScreenViewController(
                route: topLevelRoute,
                bottomSafeArea: Double(activeKeyWindowSafeAreaBottom),
                onNavigate: { route in
                    DispatchQueue.main.async {
                        coordinator.push(route)
                    }
                },
                onPopBackStack: {
                    DispatchQueue.main.async {
                        coordinator.pop()
                    }
                }
            )
        }
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
