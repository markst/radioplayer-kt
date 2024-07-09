import AVFoundation
import SwiftUI
import Combine

@objc public class MediaPlayController: NSObject, RadioPlayerType {
    internal var player: AVPlayer
    internal var cancellables: Set<AnyCancellable> = []

    // MARK: - Init

    public override init() {
        self.player = AVPlayer()
        super.init()

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

    // MARK: - RadioPlayerType

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

    // MARK: - Playback Controls

    @objc public func play(url: URL) {
        self.play(url: url, at: nil)
    }

    @objc public func play(url: URL, at position: NSNumber? = nil) {
        debugPrint(#function, url, position as Any)
        activateAudioSession()

        let playerItem = AVPlayerItem(url: url)
        player.replaceCurrentItem(with: playerItem)

        if let startTime = position?.doubleValue {
            player.seek(
                to: CMTime(seconds: startTime, preferredTimescale: CMTimeScale(NSEC_PER_SEC)),
                toleranceBefore: .zero,
                toleranceAfter: .zero
            )
        }

        player.play()
    }

    @objc public func play() {
        debugPrint(#function)
        player.play()
    }

    @objc public func pause() {
        debugPrint(#function)
        player.pause()
    }

    @objc public func stop() {
        debugPrint(#function)
        player.pause()
        player.replaceCurrentItem(with: nil)
    }

    @objc public func togglePlaying() {
        debugPrint(#function)
        if player.rate != 0 {
            pause()
        } else {
            play()
        }
    }

    @objc public func skip(_ delta: Double) {
        debugPrint(#function)
        let currentTime = player.currentTime()
        let newTime = CMTimeGetSeconds(currentTime) + delta
        seek(position: newTime)
    }

    @objc public func seek(position: TimeInterval) {
        debugPrint(#function)
        player.seek(to: CMTime(seconds: position, preferredTimescale: 1))
    }

    // MARK: - Subscriptions

    @objc public func subscribeProgress(callback: @escaping (_ progress: TimeInterval, _ duration: TimeInterval) -> Void) {
        playProgress
            .map { ($0.progress, $0.duration) }
            .sink(receiveValue: callback)
            .store(in: &cancellables)
    }

    @objc public func subscribeState(callback: @escaping (RadioPlayerState) -> Void) {
        state
            .sink(receiveValue: callback)
            .store(in: &cancellables)
    }

    @objc public func cancelSubscriptions() {
        cancellables.removeAll()
    }
}

// MARK: - Notifications

extension MediaPlayController {
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
