import Foundation
import MediaPlayer

class RemoteCommandCenter {
    
    private var radioPlayer: RadioPlayerType
    @Published
    private var state: RadioPlayerState = .stopped
    
    // MARK: - Init
    
    init(radioPlayer: RadioPlayerType) {
        self.radioPlayer = radioPlayer
        self.radioPlayer.state.assign(to: &$state)

        setupRemoteCommandCenter()
    }
    
    deinit {
        let remoteCommandCenter = MPRemoteCommandCenter.shared()
        remoteCommandCenter.togglePlayPauseCommand.removeTarget(self)
        remoteCommandCenter.playCommand.removeTarget(self)
        remoteCommandCenter.pauseCommand.removeTarget(self)
        remoteCommandCenter.stopCommand.removeTarget(self)
        remoteCommandCenter.skipForwardCommand.removeTarget(self)
        remoteCommandCenter.skipBackwardCommand.removeTarget(self)
        remoteCommandCenter.changePlaybackPositionCommand.removeTarget(self)
        
        UIApplication.shared.endReceivingRemoteControlEvents()
    }
    
    func setupRemoteCommandCenter() {
        UIApplication.shared.beginReceivingRemoteControlEvents() /// Required for `MPNowPlayingInfoCenter`

        let remoteCommandCenter = MPRemoteCommandCenter.shared()
        remoteCommandCenter.togglePlayPauseCommand.isEnabled = true
        remoteCommandCenter.togglePlayPauseCommand.addTarget { [radioPlayer] _ -> MPRemoteCommandHandlerStatus in
            radioPlayer.togglePlaying()
            return .success
        }
        remoteCommandCenter.playCommand.isEnabled = true
        remoteCommandCenter.playCommand.addTarget { [radioPlayer] event -> MPRemoteCommandHandlerStatus in
            if self.state != .playing {
                radioPlayer.play()
                return .success // Have play() return a boolean?
            } else {
                return .noActionableNowPlayingItem
            }
        }
        remoteCommandCenter.pauseCommand.isEnabled = true
        remoteCommandCenter.pauseCommand.addTarget { [radioPlayer] event -> MPRemoteCommandHandlerStatus in
            if self.state == .playing {
                radioPlayer.pause()
                return .success
            } else {
                return .noActionableNowPlayingItem
            }
        }
        remoteCommandCenter.stopCommand.isEnabled = true
        remoteCommandCenter.stopCommand.addTarget { [radioPlayer] _ -> MPRemoteCommandHandlerStatus in
            radioPlayer.stop()
            return .success
        }
        
        remoteCommandCenter.skipForwardCommand.isEnabled = true
        remoteCommandCenter.skipForwardCommand.preferredIntervals = [30]
        remoteCommandCenter.skipForwardCommand.addTarget { [radioPlayer] (event) -> MPRemoteCommandHandlerStatus in
            if let interval = (event as? MPSkipIntervalCommandEvent)?.interval {
                radioPlayer.skip(interval)
                return .success
            }
            return .commandFailed
        }
        
        remoteCommandCenter.skipBackwardCommand.isEnabled = true
        remoteCommandCenter.skipBackwardCommand.preferredIntervals = [30]
        remoteCommandCenter.skipBackwardCommand.addTarget { [radioPlayer] (event) -> MPRemoteCommandHandlerStatus in
            if let interval = (event as? MPSkipIntervalCommandEvent)?.interval {
                radioPlayer.skip(-interval)
                return .success
            }
            return .commandFailed
        }
        
        remoteCommandCenter.changePlaybackPositionCommand.isEnabled = true
        remoteCommandCenter.changePlaybackPositionCommand.addTarget { [radioPlayer] (event) -> MPRemoteCommandHandlerStatus in
            if let positionTime = (event as? MPChangePlaybackPositionCommandEvent)?.positionTime {
                radioPlayer.seek(position: positionTime)
                return .success
            }
            return .commandFailed
        }
    }
    
    /// Toggle the skip buttons visibility, such as when live stream.
    func skip(enabled: Bool) {
        let remoteCommandCenter = MPRemoteCommandCenter.shared()
        remoteCommandCenter.skipForwardCommand.isEnabled = enabled
        remoteCommandCenter.skipBackwardCommand.isEnabled = enabled
    }
}
