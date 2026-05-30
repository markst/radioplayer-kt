package dev.markturnip.radioplayer

import android.content.Intent
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

/**
 * A foreground [MediaSessionService] that hosts a [MediaSession] connected to the
 * shared [ExoPlayer] instance owned by [PlatformMediaPlayer].
 *
 * Declare this service in your app's AndroidManifest.xml:
 *
 * ```xml
 * <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
 * <uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK" />
 *
 * <service
 *     android:name="dev.markturnip.radioplayer.MediaPlayerService"
 *     android:exported="true"
 *     android:foregroundServiceType="mediaPlayback">
 *     <intent-filter>
 *         <action android:name="androidx.media3.session.MediaSessionService" />
 *     </intent-filter>
 * </service>
 * ```
 */
class MediaPlayerService : MediaSessionService() {

    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        val player = PlatformMediaPlayer.instance
            ?: error("MediaPlayerService: PlatformMediaPlayer not initialized. Call PlatformMediaPlayer.initialize(context) before starting the service.")
        mediaSession = MediaSession.Builder(this, player).build().also { addSession(it) }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? =
        mediaSession

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player
        if (player == null || !player.playWhenReady || player.mediaItemCount == 0) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        mediaSession?.let {
            removeSession(it)
            it.release()
            mediaSession = null
        }
        super.onDestroy()
    }
}
