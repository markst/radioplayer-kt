import AVFoundation
import MediaPlayer
import Combine
import UIKit.UIImage

public class NowPlayingInfo: NSObject {
    var title: String
    var artist: String?
    var artwork: UIImage?
    var duration: Double?
    
    public init(title: String, artist: String? = nil, artwork: UIImage? = nil, duration: Double? = nil) {
        self.title = title
        self.artist = artist
        self.artwork = artwork
        self.duration = duration
    }
}

public class NowPlayingInfoCenter {
    internal var cancellables: Set<AnyCancellable> = []
    
    // MARK: - Init
    
    public init(
        radioPlayer: RadioPlayerType,
        publisher: AnyPublisher<NowPlayingInfo?, Never>
    ) {
        Publishers
            .CombineLatest4(
                radioPlayer.state,
                radioPlayer.playbackState,
                radioPlayer.playProgress
                    .throttle(for: .seconds(5), scheduler: DispatchQueue.main, latest: true),
                publisher
            )
            .sink { [weak self] (state, playbackState, playProgress, info) in
                if case .failed = playbackState {
                    MPNowPlayingInfoCenter.default().nowPlayingInfo = nil
                } else {
                    self?.updateNowPlaying(info: info, state: state, progress: playProgress)
                }
            }
            .store(in: &cancellables)
        
        radioPlayer.state
            .map { state -> MPNowPlayingPlaybackState in
                switch state {
                case .paused:
                    return .paused
                case .playing, .buffering:
                    return .playing
                case .stopped:
                    return .stopped
                }
            }
            .sink {
                if #available(iOS 13.0, *) {
                    MPNowPlayingInfoCenter.default().playbackState = $0
                }
            }
            .store(in: &cancellables)
    }
    
    func updateNowPlaying(info: NowPlayingInfo?, state: RadioPlayerState, progress: Progress) {
        debugPrint(#function, info as Any, state, progress)
        guard let info else {
            MPNowPlayingInfoCenter.default().nowPlayingInfo = nil
            return
        }

        var nowPlayingInfo = [String: Any]()
        let duration = (progress.duration.isNaN || progress.duration <= 0) == false ? progress.duration : info.duration
        nowPlayingInfo[MPNowPlayingInfoPropertyIsLiveStream] = duration == nil
        // TODO: Use `player.currentTime().seconds` rather?
        nowPlayingInfo[MPNowPlayingInfoPropertyElapsedPlaybackTime] = progress.progress  /// updating this property frequently is not required (or recommended.)
        nowPlayingInfo[MPNowPlayingInfoPropertyMediaType] = NSNumber(value: MPNowPlayingInfoMediaType.audio.rawValue)

        nowPlayingInfo[MPMediaItemPropertyPlaybackDuration] = duration
        nowPlayingInfo[MPMediaItemPropertyTitle] = info.title
        nowPlayingInfo[MPMediaItemPropertyArtist] = info.artist
        nowPlayingInfo[MPMediaItemPropertyArtwork] = info.artwork.map { image in
            MPMediaItemArtwork(
                boundsSize: image.size,
                requestHandler: { _ in image }
            )
        }

        MPNowPlayingInfoCenter.default().nowPlayingInfo = nowPlayingInfo
    }
}

extension Publisher where Output == Progress {
    func filterSignficant(threshold: Double) -> AnyPublisher<Progress?, Failure> {
        scan((Progress?.none, Progress?.none)) { previous, current in
            (previous.1, current)
        }
        .filter { previous, current in
            guard let previous = previous?.progress, let current = current?.progress else { return true }
            return abs(previous - current) >= threshold
        }
        .map { $0.1 }
        .eraseToAnyPublisher()
    }
}
