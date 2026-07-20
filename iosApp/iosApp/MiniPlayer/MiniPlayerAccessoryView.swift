import SwiftUI

private enum MiniPlayerLayout {
    static let height: CGFloat = 65
}

// Native foreground is required here: tabViewBottomAccessory places SwiftUI
// content above Liquid Glass, while an embedded Compose Metal layer is sampled
// as part of the material backdrop and becomes blurred.
@available(iOS 26.1, *)
struct MiniPlayerAccessoryView: View {
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
                .frame(
                    maxWidth: .infinity,
                    minHeight: MiniPlayerLayout.height,
                    maxHeight: MiniPlayerLayout.height
                )

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
        .frame(
            maxWidth: .infinity,
            minHeight: MiniPlayerLayout.height,
            maxHeight: MiniPlayerLayout.height
        )
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
        .frame(
            maxWidth: .infinity,
            minHeight: MiniPlayerLayout.height,
            maxHeight: MiniPlayerLayout.height,
            alignment: .leading
        )
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
