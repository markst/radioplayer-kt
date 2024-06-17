import AVFoundation
import MediaPlayer
import Combine
import UIKit.UIImage

public struct NowPlayingInfo {
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

extension MediaPlayController {
    // MARK: - Remote Command Center
    
    func setupRemoteTransportControls() {
        let commandCenter = MPRemoteCommandCenter.shared()
        
        // TODO: Add seek forward and back targets
        // TODO: Add `changePlaybackPositionCommand` and toggle playback commands.
        
        commandCenter.playCommand.addTarget { [unowned self] _ in
            if self.player.rate == 0 {
                self.play()
                return .success
            }
            return .commandFailed
        }
        
        commandCenter.pauseCommand.addTarget { [unowned self] _ in
            if self.player.rate != 0 {
                self.pause()
                return .success
            }
            return .commandFailed
        }
    }
    
    // MARK: - Now Playing Info
    
    func setupNowPlaying() {


    }
    
    private func updateNowPlaying(isPause: Bool, info: NowPlayingInfo?) {
        var nowPlayingInfo = MPNowPlayingInfoCenter.default().nowPlayingInfo ?? [:]
        
        nowPlayingInfo[MPMediaItemPropertyTitle] = info?.title
        nowPlayingInfo[MPMediaItemPropertyArtist] = info?.artist
        nowPlayingInfo[MPNowPlayingInfoPropertyElapsedPlaybackTime] = player.currentTime().seconds
        nowPlayingInfo[MPNowPlayingInfoPropertyPlaybackRate] = isPause ? 0 : 1
        
        if let image = info?.artwork {
            nowPlayingInfo[MPMediaItemPropertyArtwork] = MPMediaItemArtwork(boundsSize: image.size) { size in
                return image
            }
        }
        
        MPNowPlayingInfoCenter.default().nowPlayingInfo = nowPlayingInfo

    }
    
    // MARK: - Observers
    
    public func setupObservers(publisher: AnyPublisher<NowPlayingInfo?, Never>) {
        // TODO: Combine state or did seek in order to trigger `nowPlayingInfo` on significant play head change.
        Publishers.CombineLatest(player.publisher(for: \.timeControlStatus), publisher)
            .sink { [weak self] (status, info) in
                self?.updateNowPlaying(isPause: status == .playing, info: info)
            }
            .store(in: &cancellables)
    }
    
}
