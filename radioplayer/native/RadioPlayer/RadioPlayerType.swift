import Combine
import Foundation
import AVFoundation

public protocol RadioPlayerType {
    var state: AnyPublisher<RadioPlayerState, Never> { get }
    var playbackState: AnyPublisher<RadioPlayerPlaybackState, Never> { get }
    var playProgress: AnyPublisher<Progress, Never> { get }
    var isPlaying: AnyPublisher<Bool, Never> { get }
    
    func play(url: URL)
    func play(url: URL, at position: NSNumber?)
    func play()
    func pause()
    func stop()
    func togglePlaying()

    func skip(_ delta: Double)
    func seek(position: TimeInterval)

    // TODO: Separate from `MediaPlayController` rather than extension.
    func setupObservers(publisher: AnyPublisher<NowPlayingInfo?, Never>)
}

public struct Progress {
    public let id: Int
    public let progress: TimeInterval
    public let duration: TimeInterval
}
