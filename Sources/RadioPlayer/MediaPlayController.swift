import AVFoundation
import SwiftUI
import Combine

public enum PlayerState {
    case stopped
    case playing
    case paused
    case buffering
    case readyToPlay
    case failed
}

public class MediaPlayController: ObservableObject, RadioPlayerType {
    internal var player: AVPlayer
    internal var cancellables: Set<AnyCancellable> = []
    
    // Combine publishers
        
    public var playerState: AnyPublisher<PlayerState, Never> {
        let currentItemStatus = player.publisher(for: \.currentItem)
            .compactMap { $0 }
            .flatMap { item in
                Publishers.CombineLatest4(
                    item.publisher(for: \.status),
                    item.publisher(for: \.isPlaybackLikelyToKeepUp),
                    item.publisher(for: \.isPlaybackBufferEmpty),
                    item.publisher(for: \.isPlaybackBufferFull)
                )
            }
            .eraseToAnyPublisher()
        
        return Publishers.CombineLatest3(
            player.publisher(for: \.timeControlStatus),
            player.publisher(for: \.status),
            currentItemStatus
        )
        .map { timeControlStatus, playerStatus, itemStatus in
            switch (playerStatus, timeControlStatus, itemStatus.0, itemStatus.1, itemStatus.2, itemStatus.3) {
            case (.failed, _, _, _, _, _):
                return .failed
            case (.readyToPlay, _, _, _, _, _):
                return .readyToPlay
            case (_, .waitingToPlayAtSpecifiedRate, _, _, _, _):
                return .buffering
            case (_, .playing, .readyToPlay, true, false, true):
                return .playing
            case (_, .paused, _, _, _, _):
                return .paused
            case (_, _, _, false, true, false):
                return .buffering
            default:
                return .stopped
            }
        }
        .eraseToAnyPublisher()
    }
    
    public var state: AnyPublisher<RadioPlayerState, Never> {
        player.publisher(for: \.timeControlStatus)
            .map { status in
                switch status {
                case .paused:
                    return .paused
                case .waitingToPlayAtSpecifiedRate:
                    return .buffering
                case .playing: // check rate?
                    return .playing
                @unknown default:
                    return .stopped
                }
            }
            .print("timeControlStatus")
            .eraseToAnyPublisher()
    }
    
    public var playbackState: AnyPublisher<RadioPlayerPlaybackState, Never> {
        player.publisher(for: \.status)
            .map { status in
                switch status {
                case .readyToPlay:
                    return .readyToPlay
                case .failed:
                    return .failed
                case .unknown:
                    return .buffering
                @unknown default:
                    return .buffering
                }
            }
            .print("status")
            .eraseToAnyPublisher()
    }

    public var playProgress: AnyPublisher<Progress, Never> {
        player
            .publisher(for: \.currentItem)
            .compactMap { $0 }
            .map { [player] item in
                Publishers
                    .CombineLatest(
                        player
                            .periodicTimePublisher()
                            .prepend(.zero),
                        item.publisher(for: \.duration)
                            .compactMap({ $0 })
                    )
                    .map {
                        Progress(
                            id: item.hash,
                            progress: player.currentTime().seconds,
                            duration: $0.1.seconds
                        )
                    }
            }
            .switchToLatest()
            .print("playProgress")
            .eraseToAnyPublisher()
    }
    
    public var isPlaying: AnyPublisher<Bool, Never> {
        player.publisher(for: \.rate)
            .map { $0 != 0 }
            .eraseToAnyPublisher()
    }
    
    public var rate: AnyPublisher<Float?, Never> {
        player.publisher(for: \.rate)
            .map { Optional($0) }
            .eraseToAnyPublisher()
    }

    // MARK: - Init
    
    public init() {
        self.player = AVPlayer()
        setupRemoteTransportControls()
        setupNotifications()
        
        // TODO: Figure out why I was looking up current outputs:
        // AVAudioSession.sharedInstance().currentRoute.outputs.first?.portType == .

        // TODO: current item on `AVPlayerItemDidPlayToEndTime`?

        // TODO: Handle current item error state:
        // player.publisher(for: \.currentItem)
        //     .compactMap { $0?.publisher(for: \.error) }
        //     .switchToLatest()
    }
        
    private var audioSession: AVAudioSession {
        AVAudioSession.sharedInstance()
    }
    
    func activateAudioSession() {
        do {
            try audioSession.setCategory(.playback, mode: .default, policy: .longFormAudio)
            try audioSession.setActive(true)
        } catch {
            print("Error in setting up audio session \(error)")
        }
    }
    
    // MARK: - Playback Controls
    
    public func play(url: URL, at position: CMTime? = nil) {
        debugPrint(#function)
        activateAudioSession()
        
        let playerItem = AVPlayerItem(url: url)
        player.replaceCurrentItem(with: playerItem)

        if let startTime = position {
            player.seek(to: startTime, toleranceBefore: .zero, toleranceAfter: .zero)
        }

        player.play()
    }
    
    public func play() {
        debugPrint(#function)
        player.play()
    }

    public func pause() {
        debugPrint(#function)
        player.pause()
    }
    
    public func stop() {
        debugPrint(#function)
        player.pause()
        player.replaceCurrentItem(with: nil)
    }
    
    public func togglePlaying() {
        debugPrint(#function)
        if player.rate != 0 {
            pause()
        } else {
            play()
        }
    }
    
    public func seek(delta: Double) {
        debugPrint(#function)
        let currentTime = player.currentTime()
        let newTime = CMTimeGetSeconds(currentTime) + delta
        seek(to: newTime)
    }
    
    public func seek(to position: TimeInterval) {
        debugPrint(#function)
        player.seek(to: CMTime(seconds: position, preferredTimescale: 1))
    }
    
    public func skip(stepInterval: TimeInterval) {
        debugPrint(#function)
        seek(delta: stepInterval)
    }
}

extension MediaPlayController {
    
    // MARK: - Notifications
    
    func setupNotifications() {
        let notificationCenter = NotificationCenter.default
        // TODO: Replace with `publisher(for: AVAudioSession.x)`
        notificationCenter.addObserver(self,
                                       selector: #selector(handleInterruption),
                                       name: AVAudioSession.interruptionNotification,
                                       object: nil)
        notificationCenter.addObserver(self,
                                       selector: #selector(handleRouteChange),
                                       name: AVAudioSession.routeChangeNotification,
                                       object: nil)
    }
    
    @objc private func handleInterruption(notification: Notification) {
        guard let userInfo = notification.userInfo,
              let typeValue = userInfo[AVAudioSessionInterruptionTypeKey] as? UInt,
              let type = AVAudioSession.InterruptionType(rawValue: typeValue) else { return }
        
        if type == .began {
            pause()
        } else if type == .ended {
            guard let optionsValue = userInfo[AVAudioSessionInterruptionOptionKey] as? UInt else { return }
            let options = AVAudioSession.InterruptionOptions(rawValue: optionsValue)
            if options.contains(.shouldResume) {
                play()
            }
        }
    }
    
    @objc private func handleRouteChange(notification: Notification) {
        guard let userInfo = notification.userInfo,
              let reasonValue = userInfo[AVAudioSessionRouteChangeReasonKey] as? UInt,
              let reason = AVAudioSession.RouteChangeReason(rawValue: reasonValue) else { return }
        
        switch reason {
        case .newDeviceAvailable:
            let session = AVAudioSession.sharedInstance()
            for output in session.currentRoute.outputs where output.portType == .headphones {
                play()
                break
            }
        case .oldDeviceUnavailable:
            if let previousRoute = userInfo[AVAudioSessionRouteChangePreviousRouteKey] as? AVAudioSessionRouteDescription {
                for output in previousRoute.outputs where output.portType == .headphones {
                    // TODO: Check headphone state?
                    pause()
                    break
                }
            }
        default:
            break
        }
    }
}
