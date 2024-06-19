import Combine
import Foundation
import AVFoundation

@objc public protocol MediaPlayerType {
    func play(url: URL)
    func play()
    func pause()
    func stop()
    func togglePlaying()
}

public protocol RadioPlayerType {
    var state: AnyPublisher<RadioPlayerState, Never> { get }
    var playbackState: AnyPublisher<RadioPlayerPlaybackState, Never> { get }
    var playProgress: AnyPublisher<Progress, Never> { get }
    var isPlaying: AnyPublisher<Bool, Never> { get }
    
    func play(url: URL, at position: CMTime?)
    func play()
    func pause()
    func stop()
    func togglePlaying()
    
    func seek(delta: Double)
    func seek(to position: TimeInterval)
    func skip(stepInterval: TimeInterval)
    
    // TODO: Separate from `MediaPlayController` rather than extension.
    func setupObservers(publisher: AnyPublisher<NowPlayingInfo?, Never>)
}

public struct Progress {
    public let id: Int
    public let progress: TimeInterval
    public let duration: TimeInterval
}
