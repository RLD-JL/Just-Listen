import SwiftUI
import shared

// Wrapper to make Kotlin Route conform to Hashable and Identifiable for NavigationStack
@available(iOS 17.0, *)
struct RouteWrapper: Hashable, Identifiable {
    let id = UUID()
    let route: Route

    static func ==(lhs: RouteWrapper, rhs: RouteWrapper) -> Bool {
        lhs.id == rhs.id
    }

    func hash(into hasher: inout Hasher) {
        hasher.combine(id)
    }
}

// Keep Compose controllers alive while their routes remain in a native
// NavigationStack. Recreating a controller also recreates its composition,
// which briefly puts every Coil painter back into its loading state.
@available(iOS 17.0, *)
fileprivate final class ComposeControllerStore {
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

// Coordinator for individual Tab navigation path
@available(iOS 17.0, *)
@Observable
class TabNavigationCoordinator {
    fileprivate let controllerStore = ComposeControllerStore()
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

// App-wide navigation and tab selection coordinator
@available(iOS 17.0, *)
@Observable
class AppNavigationCoordinator {
    enum AppTab {
        case library, playlists, feed, search, settings
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

// UIViewControllerRepresentable to host single Compose screen
@available(iOS 17.0, *)
struct DetailComposeView: UIViewControllerRepresentable {
    let routeID: UUID
    let route: Route
    let coordinator: TabNavigationCoordinator

    func makeUIViewController(context: Context) -> UIViewController {
        let keyWindow = UIApplication.shared.connectedScenes
            .filter { $0.activationState == .foregroundActive }
            .compactMap { $0 as? UIWindowScene }
            .first?.windows
            .first { $0.isKeyWindow }
        let safeAreaBottom = keyWindow?.safeAreaInsets.bottom ?? 0

        return coordinator.controllerStore.detail(id: routeID) {
            ScreenHosts_iosKt.ScreenViewController(
                route: route,
                bottomSafeArea: Double(safeAreaBottom),
                onNavigate: { newRoute in
                    DispatchQueue.main.async {
                        self.coordinator.push(newRoute)
                    }
                },
                onPopBackStack: {
                    DispatchQueue.main.async {
                        self.coordinator.pop()
                    }
                }
            )
        }
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

// UIViewControllerRepresentable to host the tab root
@available(iOS 17.0, *)
struct NativeNavComposeView: UIViewControllerRepresentable {
    let topLevelRoute: Route
    let coordinator: TabNavigationCoordinator
    let appCoordinator: AppNavigationCoordinator

    func makeUIViewController(context: Context) -> UIViewController {
        let keyWindow = UIApplication.shared.connectedScenes
            .filter { $0.activationState == .foregroundActive }
            .compactMap { $0 as? UIWindowScene }
            .first?.windows
            .first { $0.isKeyWindow }
        let safeAreaBottom = keyWindow?.safeAreaInsets.bottom ?? 0

        return coordinator.controllerStore.root {
            ScreenHosts_iosKt.ScreenViewController(
                route: topLevelRoute,
                bottomSafeArea: Double(safeAreaBottom),
                onNavigate: { route in
                    DispatchQueue.main.async {
                        self.coordinator.push(route)
                    }
                },
                onPopBackStack: {
                    DispatchQueue.main.async {
                        self.coordinator.pop()
                    }
                }
            )
        }
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

// UIViewControllerRepresentable to host the global music player
@available(iOS 17.0, *)
struct GlobalPlayerComposeView: UIViewControllerRepresentable {
    let initialExpanded: Bool
    let onNavigate: (Route) -> Void
    let onHeightChanged: (Bool) -> Void
    let onVisibilityChanged: (Bool) -> Void

    func makeUIViewController(context: Context) -> UIViewController {
        let vc = ScreenHosts_iosKt.PlayerViewController(
            initialExpanded: initialExpanded,
            onNavigate: { route in
                DispatchQueue.main.async {
                    self.onNavigate(route)
                }
            },
            onHeightChanged: { expanded in
                DispatchQueue.main.async {
                    self.onHeightChanged(expanded.boolValue)
                }
            },
            onVisibilityChanged: { visible in
                DispatchQueue.main.async {
                    self.onVisibilityChanged(visible.boolValue)
                }
            }
        )
        vc.view.backgroundColor = .clear
        return vc
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

private struct MiniTrackPresentationState: Equatable {
    var id = ""
    var title = ""
    var artist = ""
    var artworkURL = ""

    init() {}

    init(_ state: IosMiniTrackState) {
        id = state.id
        title = state.title
        artist = state.artist
        artworkURL = state.artworkUrl
    }
}

private struct MiniPlayerPresentationState: Equatable {
    var visible = false
    var currentTrack: MiniTrackPresentationState?
    var previousTrack: MiniTrackPresentationState?
    var nextTrack: MiniTrackPresentationState?
    var isPlaying = false
    var isBuffering = false
}

private final class ArtworkImageCache {
    static let shared = ArtworkImageCache()
    private let images = NSCache<NSString, UIImage>()

    func image(for urlString: String) -> UIImage? {
        images.object(forKey: urlString as NSString)
    }

    func insert(_ image: UIImage, for urlString: String) {
        images.setObject(image, forKey: urlString as NSString)
    }
}

private struct CachedArtworkImage: View {
    let urlString: String
    @State private var image: UIImage?

    init(urlString: String) {
        self.urlString = urlString
        _image = State(initialValue: ArtworkImageCache.shared.image(for: urlString))
    }

    var body: some View {
        Group {
            if let image {
                Image(uiImage: image)
                    .resizable()
                    .scaledToFill()
            } else {
                RoundedRectangle(cornerRadius: 7, style: .continuous)
                    .fill(Color.primary.opacity(0.10))
                    .overlay {
                        Image(systemName: "music.note")
                            .foregroundStyle(.secondary)
                    }
            }
        }
        .task(id: urlString) {
            if let cached = ArtworkImageCache.shared.image(for: urlString) {
                image = cached
                return
            }

            image = nil
            guard let url = URL(string: urlString), !urlString.isEmpty else { return }

            do {
                let (data, _) = try await URLSession.shared.data(from: url)
                guard !Task.isCancelled, let loadedImage = UIImage(data: data) else { return }
                ArtworkImageCache.shared.insert(loadedImage, for: urlString)
                image = loadedImage
            } catch {
                // Keep the neutral placeholder when artwork is genuinely unavailable.
            }
        }
    }
}

// Playback stays observable even while the conditional tab accessory is absent.
@available(iOS 26.1, *)
private struct PlayerStateObserverComposeView: UIViewControllerRepresentable {
    let onStateChanged: (MiniPlayerPresentationState) -> Void

    func makeUIViewController(context: Context) -> UIViewController {
        let vc = ScreenHosts_iosKt.PlayerStateObserverViewController(
            onStateChanged: { state in
                DispatchQueue.main.async {
                    self.onStateChanged(
                        MiniPlayerPresentationState(
                            visible: state.visible,
                            currentTrack: state.currentTrack.map(MiniTrackPresentationState.init),
                            previousTrack: state.previousTrack.map(MiniTrackPresentationState.init),
                            nextTrack: state.nextTrack.map(MiniTrackPresentationState.init),
                            isPlaying: state.isPlaying,
                            isBuffering: state.isBuffering
                        )
                    )
                }
            }
        )
        vc.view.backgroundColor = .clear
        return vc
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

// Native foreground is required here: tabViewBottomAccessory places SwiftUI
// content above Liquid Glass, while an embedded Compose Metal layer is sampled
// as part of the material backdrop and becomes blurred.
@available(iOS 26.1, *)
private struct MiniPlayerAccessoryView: View {
    private enum SwipeDirection {
        case previous
        case next
    }

    let state: MiniPlayerPresentationState
    let onExpand: () -> Void
    let onPlayPause: () -> Void
    let onSkipNext: () -> Void
    let onSkipPrevious: () -> Void

    @State private var carouselOffset: CGFloat = 0
    @State private var optimisticTrack: MiniTrackPresentationState?
    @State private var isTransitioning = false

    var body: some View {
        HStack(spacing: 8) {
            trackCarousel
                .frame(maxWidth: .infinity, minHeight: 65, maxHeight: 65)

            Button(action: onPlayPause) {
                Group {
                    if state.isBuffering {
                        ProgressView()
                            .controlSize(.small)
                    } else {
                        Image(systemName: state.isPlaying ? "pause.fill" : "play.fill")
                            .font(.system(size: 20, weight: .semibold))
                    }
                }
                .frame(width: 36, height: 44)
                .contentShape(Rectangle())
            }
            .buttonStyle(.plain)
            .accessibilityLabel(state.isPlaying ? "Pause" : "Play")

            Button(action: onSkipNext) {
                Image(systemName: "forward.end.fill")
                    .font(.system(size: 19, weight: .semibold))
                    .frame(width: 36, height: 44)
                    .contentShape(Rectangle())
            }
            .buttonStyle(.plain)
            .accessibilityLabel("Next track")
        }
        .padding(.horizontal, 12)
        .frame(maxWidth: .infinity, minHeight: 65, maxHeight: 65)
        .accessibilityElement(children: .contain)
        .onChange(of: state.currentTrack?.id) { _, currentID in
            guard optimisticTrack?.id == currentID else { return }
            optimisticTrack = nil
            isTransitioning = false
        }
    }

    @ViewBuilder
    private var trackCarousel: some View {
        GeometryReader { proxy in
            let width = max(proxy.size.width, 1)
            let currentTrack = optimisticTrack ?? state.currentTrack ?? MiniTrackPresentationState()

            ZStack(alignment: .leading) {
                if let previousTrack = state.previousTrack {
                    trackRow(previousTrack)
                        .offset(x: -width + carouselOffset)
                }

                trackRow(currentTrack)
                    .offset(x: carouselOffset)

                if let nextTrack = state.nextTrack {
                    trackRow(nextTrack)
                        .offset(x: width + carouselOffset)
                }
            }
            .frame(width: width, height: proxy.size.height, alignment: .leading)
            .clipped()
            .contentShape(Rectangle())
            .onTapGesture(perform: onExpand)
            .highPriorityGesture(
                DragGesture(minimumDistance: 12)
                    .onChanged { value in
                        guard !isTransitioning else { return }
                        let horizontal = value.translation.width
                        let vertical = value.translation.height
                        guard abs(horizontal) > abs(vertical) else {
                            carouselOffset = 0
                            return
                        }

                        if horizontal > 0, state.previousTrack == nil {
                            carouselOffset = horizontal * 0.12
                        } else if horizontal < 0, state.nextTrack == nil {
                            carouselOffset = horizontal * 0.12
                        } else {
                            carouselOffset = horizontal
                        }
                    }
                    .onEnded { value in
                        guard !isTransitioning else { return }
                        let horizontal = value.translation.width
                        let vertical = value.translation.height
                        let projected = value.predictedEndTranslation.width
                        let threshold = width * 0.25

                        if abs(horizontal) > abs(vertical), projected < -threshold,
                           let nextTrack = state.nextTrack {
                            completeSwipe(.next, track: nextTrack, width: width)
                        } else if abs(horizontal) > abs(vertical), projected > threshold,
                                  let previousTrack = state.previousTrack {
                            completeSwipe(.previous, track: previousTrack, width: width)
                        } else {
                            withAnimation(.spring(response: 0.28, dampingFraction: 0.86)) {
                                carouselOffset = 0
                            }
                            if abs(vertical) > abs(horizontal), vertical < -36 {
                                onExpand()
                            }
                        }
                    }
            )
        }
    }

    private func trackRow(_ track: MiniTrackPresentationState) -> some View {
        HStack(spacing: 10) {
            CachedArtworkImage(urlString: track.artworkURL)
                .frame(width: 47, height: 47)
                .clipShape(RoundedRectangle(cornerRadius: 7, style: .continuous))
                .accessibilityHidden(true)

            VStack(alignment: .leading, spacing: 2) {
                Text(track.title.isEmpty ? "Now Playing" : track.title)
                    .font(.system(size: 14, weight: .semibold))
                    .foregroundStyle(.primary)
                    .lineLimit(1)

                if !track.artist.isEmpty {
                    Text(track.artist)
                        .font(.system(size: 11, weight: .regular))
                        .foregroundStyle(.secondary)
                        .lineLimit(1)
                }
            }
            .frame(maxWidth: .infinity, alignment: .leading)
        }
        .frame(maxWidth: .infinity, minHeight: 65, maxHeight: 65, alignment: .leading)
    }

    private func completeSwipe(
        _ direction: SwipeDirection,
        track: MiniTrackPresentationState,
        width: CGFloat
    ) {
        isTransitioning = true
        let destination = direction == .next ? -width : width

        withAnimation(.easeOut(duration: 0.18)) {
            carouselOffset = destination
        }

        DispatchQueue.main.asyncAfter(deadline: .now() + 0.18) {
            optimisticTrack = track
            var transaction = Transaction()
            transaction.disablesAnimations = true
            withTransaction(transaction) {
                carouselOffset = 0
            }

            switch direction {
            case .previous:
                onSkipPrevious()
            case .next:
                onSkipNext()
            }

            // Avoid permanently locking the gesture if playback fails to emit
            // a new current item for any reason.
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.8) {
                isTransitioning = false
            }
        }
    }
}

// Map Route to title in Swift
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

// View for tab content hosting the NavigationStack
@available(iOS 17.0, *)
struct TabContentView: View {
    let topLevelRoute: Route
    let coordinator: TabNavigationCoordinator
    let appCoordinator: AppNavigationCoordinator
    let title: String

    var body: some View {
        NavigationStack(path: Binding(
            get: { coordinator.path },
            set: { coordinator.path = $0 }
        )) {
            NativeNavComposeView(
                topLevelRoute: topLevelRoute,
                coordinator: coordinator,
                appCoordinator: appCoordinator
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

// Native navigation view for iOS 26+ containing TabView
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
                    appCoordinator: appCoordinator,
                    title: "Playlists"
                )
            }
            Tab("Library", systemImage: "books.vertical.fill", value: AppNavigationCoordinator.AppTab.library) {
                TabContentView(
                    topLevelRoute: Route.Library.shared,
                    coordinator: appCoordinator.libraryCoordinator,
                    appCoordinator: appCoordinator,
                    title: "Library"
                )
            }
            Tab("Feed", systemImage: "newspaper.fill", value: AppNavigationCoordinator.AppTab.feed) {
                TabContentView(
                    topLevelRoute: Route.Feed(category: nil, timeRange: nil),
                    coordinator: appCoordinator.feedCoordinator,
                    appCoordinator: appCoordinator,
                    title: "Feed"
                )
            }
            Tab("Search", systemImage: "magnifyingglass", value: AppNavigationCoordinator.AppTab.search) {
                TabContentView(
                    topLevelRoute: Route.Search.shared,
                    coordinator: appCoordinator.searchCoordinator,
                    appCoordinator: appCoordinator,
                    title: "Search"
                )
            }
            Tab("Settings", systemImage: "gearshape.fill", value: AppNavigationCoordinator.AppTab.settings) {
                TabContentView(
                    topLevelRoute: Route.Settings.shared,
                    coordinator: appCoordinator.settingsCoordinator,
                    appCoordinator: appCoordinator,
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
                    ScreenHosts_iosKt.iosPlayerTogglePlayback()
                },
                onSkipNext: {
                    ScreenHosts_iosKt.iosPlayerSkipToNext()
                },
                onSkipPrevious: {
                    ScreenHosts_iosKt.iosPlayerSkipToPrevious()
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

// Pre-iOS 26 fallback view
struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        return ScreenHosts_iosKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    @AppStorage("useLiquidGlassNavigation") private var useLiquidGlassNavigation = true

    var body: some View {
        Group {
            if #available(iOS 26.1, *), useLiquidGlassNavigation {
                NativeNavContentView()
                    .onOpenURL { url in
                        handleDeepLink(url)
                    }
            } else {
                ComposeView()
                    .ignoresSafeArea(.all)
                    .onOpenURL { url in
                        handleDeepLink(url)
                    }
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
