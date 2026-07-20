import Foundation
import SwiftUI
import shared

struct MiniTrackPresentationState: Equatable {
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

struct MiniPlayerPresentationState: Equatable {
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

struct CachedArtworkImage: View {
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
struct PlayerStateObserverComposeView: UIViewControllerRepresentable {
    let onStateChanged: (MiniPlayerPresentationState) -> Void

    func makeUIViewController(context: Context) -> UIViewController {
        let vc = IosMiniPlayerBridgeKt.PlayerStateObserverViewController(
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
