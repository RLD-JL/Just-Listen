import SwiftUI
import shared

func getRouteTitle(_ route: Route) -> String {
    if route is Route.Library { return "Library" }
    if route is Route.Playlist { return "Playlists" }
    if route is Route.Feed { return "Feed" }
    if route is Route.Search { return "Search" }
    if route is Route.Settings { return "Settings" }
    if route is Route.Support { return "Support" }
    if let detail = route as? Route.PlaylistDetail { return detail.playlistTitle }
    if let seeAll = route as? Route.SeeAll { return seeAll.categoryName }
    if let artist = route as? Route.ArtistProfile { return artist.artistName }
    if route is Route.Notifications { return "Notifications" }
    if route is Route.ArtistDashboard { return "Artist Dashboard" }
    if route is Route.CustomTheme { return "Custom Theme" }
    if route is Route.MusicInsights { return "Music Insights" }
    return ""
}

@available(iOS 17.0, *)
struct TabContentView: View {
    let topLevelRoute: Route
    let coordinator: TabNavigationCoordinator
    let title: String

    var body: some View {
        NavigationStack(path: Binding(
            get: { coordinator.path },
            set: { coordinator.path = $0 }
        )) {
            NativeNavComposeView(
                topLevelRoute: topLevelRoute,
                coordinator: coordinator
            )
            .ignoresSafeArea(.all)
            .navigationTitle(title)
            .toolbar(.hidden, for: .navigationBar)
            .navigationDestination(for: RouteWrapper.self) { wrapper in
                DetailComposeView(
                    routeID: wrapper.id,
                    route: wrapper.route,
                    coordinator: coordinator
                )
                .ignoresSafeArea(.all)
                .navigationTitle(getRouteTitle(wrapper.route))
                .toolbar(.visible, for: .navigationBar)
                .toolbarTitleDisplayMode(.inline)
            }
        }
    }
}

@available(iOS 26.1, *)
struct NativeNavContentView: View {
    @State private var appCoordinator = AppNavigationCoordinator()
    @State private var playerExpanded = false
    @State private var miniPlayerState = MiniPlayerPresentationState()

    var body: some View {
        TabView(selection: Binding(
            get: { appCoordinator.selectedTab },
            set: { appCoordinator.selectedTab = $0 }
        )) {
            Tab("Playlists", systemImage: "music.note.list", value: AppNavigationCoordinator.AppTab.playlists) {
                TabContentView(
                    topLevelRoute: Route.Playlist.shared,
                    coordinator: appCoordinator.playlistsCoordinator,
                    title: "Playlists"
                )
            }
            Tab("Library", systemImage: "books.vertical.fill", value: AppNavigationCoordinator.AppTab.library) {
                TabContentView(
                    topLevelRoute: Route.Library.shared,
                    coordinator: appCoordinator.libraryCoordinator,
                    title: "Library"
                )
            }
            Tab("Feed", systemImage: "newspaper.fill", value: AppNavigationCoordinator.AppTab.feed) {
                TabContentView(
                    topLevelRoute: Route.Feed(category: nil, timeRange: nil),
                    coordinator: appCoordinator.feedCoordinator,
                    title: "Feed"
                )
            }
            Tab("Search", systemImage: "magnifyingglass", value: AppNavigationCoordinator.AppTab.search) {
                TabContentView(
                    topLevelRoute: Route.Search.shared,
                    coordinator: appCoordinator.searchCoordinator,
                    title: "Search"
                )
            }
            Tab("Settings", systemImage: "gearshape.fill", value: AppNavigationCoordinator.AppTab.settings) {
                TabContentView(
                    topLevelRoute: Route.Settings.shared,
                    coordinator: appCoordinator.settingsCoordinator,
                    title: "Settings"
                )
            }
        }
        .tabBarMinimizeBehavior(.never)
        .tabViewBottomAccessory(isEnabled: miniPlayerState.visible && !playerExpanded) {
            MiniPlayerAccessoryView(
                state: miniPlayerState,
                onExpand: {
                    self.playerExpanded = true
                },
                onPlayPause: {
                    IosMiniPlayerBridgeKt.iosPlayerTogglePlayback()
                },
                onSkipNext: {
                    IosMiniPlayerBridgeKt.iosPlayerSkipToNext()
                },
                onSkipPrevious: {
                    IosMiniPlayerBridgeKt.iosPlayerSkipToPrevious()
                }
            )
        }
        .background {
            PlayerStateObserverComposeView { state in
                if self.miniPlayerState != state {
                    self.miniPlayerState = state
                }
            }
            .frame(width: 1, height: 1)
            .allowsHitTesting(false)
        }
        .fullScreenCover(isPresented: $playerExpanded) {
            GlobalPlayerComposeView(
                initialExpanded: true,
                onNavigate: { route in
                    self.playerExpanded = false
                    switch appCoordinator.selectedTab {
                    case .playlists: appCoordinator.playlistsCoordinator.push(route)
                    case .library: appCoordinator.libraryCoordinator.push(route)
                    case .feed: appCoordinator.feedCoordinator.push(route)
                    case .search: appCoordinator.searchCoordinator.push(route)
                    case .settings: appCoordinator.settingsCoordinator.push(route)
                    }
                },
                onHeightChanged: { expanded in
                    self.playerExpanded = expanded
                },
                onVisibilityChanged: { _ in }
            )
            .ignoresSafeArea(.all)
            .presentationBackground(.black)
        }
    }
}
