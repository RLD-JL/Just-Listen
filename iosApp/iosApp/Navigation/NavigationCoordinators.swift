import SwiftUI
import shared

@available(iOS 17.0, *)
struct RouteWrapper: Hashable, Identifiable {
    let id = UUID()
    let route: Route

    static func == (lhs: RouteWrapper, rhs: RouteWrapper) -> Bool {
        lhs.id == rhs.id
    }

    func hash(into hasher: inout Hasher) {
        hasher.combine(id)
    }
}

/// Retains Compose controllers while their routes remain in a native
/// `NavigationStack`, preventing image painters and screen state from resetting.
@available(iOS 17.0, *)
final class ComposeControllerStore {
    private var rootController: UIViewController?
    private var detailControllers: [UUID: UIViewController] = [:]

    func root(make: () -> UIViewController) -> UIViewController {
        if let rootController, rootController.parent == nil {
            return rootController
        }

        let controller = make()
        rootController = controller
        return controller
    }

    func detail(id: UUID, make: () -> UIViewController) -> UIViewController {
        if let controller = detailControllers[id], controller.parent == nil {
            return controller
        }

        let controller = make()
        detailControllers[id] = controller
        return controller
    }

    func retainDetails(withIDs ids: Set<UUID>) {
        detailControllers = detailControllers.filter { ids.contains($0.key) }
    }
}

@available(iOS 17.0, *)
@Observable
final class TabNavigationCoordinator {
    let controllerStore = ComposeControllerStore()

    var path: [RouteWrapper] = [] {
        didSet {
            controllerStore.retainDetails(withIDs: Set(path.map(\.id)))
        }
    }

    func push(_ route: Route) {
        path.append(RouteWrapper(route: route))
    }

    func pop() {
        if !path.isEmpty {
            path.removeLast()
        }
    }

    func popToRoot() {
        path.removeAll()
    }
}

@available(iOS 17.0, *)
@Observable
final class AppNavigationCoordinator {
    enum AppTab {
        case library
        case playlists
        case feed
        case search
        case settings
    }

    var selectedTab: AppTab = .playlists
    let libraryCoordinator = TabNavigationCoordinator()
    let playlistsCoordinator = TabNavigationCoordinator()
    let feedCoordinator = TabNavigationCoordinator()
    let searchCoordinator = TabNavigationCoordinator()
    let settingsCoordinator = TabNavigationCoordinator()

    func activateTab(for route: Route) {
        if route is Route.Library {
            selectedTab = .library
        } else if route is Route.Playlist {
            selectedTab = .playlists
        } else if route is Route.Feed {
            selectedTab = .feed
        } else if route is Route.Search {
            selectedTab = .search
        } else if route is Route.Settings {
            selectedTab = .settings
        }
    }
}
