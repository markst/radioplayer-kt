import AVFoundation
import Combine

extension AVPlayer {
    func periodicTimePublisher(
        forInterval interval: CMTime = CMTime(seconds: 0.5, preferredTimescale: CMTimeScale(NSEC_PER_SEC)),
        queue: DispatchQueue? = nil
    ) -> AnyPublisher<CMTime, Never> {
        Publisher(
            self,
            forInterval: interval,
            queue: queue
        )
        .eraseToAnyPublisher()
    }
}

extension AVPlayer {
    fileprivate struct Publisher: Combine.Publisher {
        
        typealias Output = CMTime
        typealias Failure = Never
        
        var player: AVPlayer
        var interval: CMTime
        var queue: DispatchQueue?
        
        init(
            _ player: AVPlayer,
            forInterval interval: CMTime,
            queue: DispatchQueue?
        ) {
            self.player = player
            self.interval = interval
            self.queue = queue
        }
        
        func receive<S>(subscriber: S) where S: Subscriber, Publisher.Failure == S.Failure, Publisher.Output == S.Input {
            let subscription = CMTime.Subscription(
                subscriber: subscriber,
                player: player,
                forInterval: interval,
                queue: queue
            )
            subscriber.receive(subscription: subscription)
        }
    }
}

extension CMTime {
    fileprivate final class Subscription<SubscriberType: Subscriber>: Combine.Subscription where SubscriberType.Input == CMTime, SubscriberType.Failure == Never {
        
        var player: AVPlayer?
        var observer: Any?
        
        init(
            subscriber: SubscriberType,
            player: AVPlayer,
            forInterval interval: CMTime,
            queue: DispatchQueue?
        ) {
            self.player = player
            observer = player.addPeriodicTimeObserver(
                forInterval: interval,
                queue: queue
            ) { time in
                _ = subscriber.receive(time)
            }
        }
        
        func request(_: Subscribers.Demand) {
            // We do nothing here as we only want to send events when they occur.
        }
        
        func cancel() {
            if let observer = observer {
                player?.removeTimeObserver(observer)
            }
            observer = nil
            player = nil
        }
    }
}
