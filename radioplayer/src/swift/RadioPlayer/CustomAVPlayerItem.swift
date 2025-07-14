import Foundation
import AVFoundation

class CustomAVPlayerItem: AVPlayerItem {
    let info: NowPlayingInfo?

    init(url: URL, info: NowPlayingInfo?) {
        self.info = info
        super.init(asset: .init(url: url), automaticallyLoadedAssetKeys: nil)
        self.canUseNetworkResourcesForLiveStreamingWhilePaused = false
    }
}
