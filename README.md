# RadioPlayer

Demonstrating a multipurpose Kotlin Multiplatform and Swift Package audio player.

**RadioPlayer** combines shared Kotlin logic with native implementations using [ExoPlayer](https://developer.android.com/media/media3/exoplayer) for Android and [AVPlayer](https://developer.apple.com/documentation/avfoundation/avplayer/) in Swift for iOS, showcasing the future potential of Kotlin Multiplatform packages.

This repository demonstrates how a package can be structured to share implementation between:
- Kotlin Android Package
- Kotlin Android Multiplatform Package
- Kotlin iOS Multiplatform Package
- Swift Package

By leveraging the native Kotlin SwiftPM import feature (Kotlin 2.4.0-titan-214) for importing Swift code into the Kotlin Multiplatform Mobile shared module, the iOS RadioPlayer implementation is native Swift code rather than using interpolation. This means we can serve the Swift code as its own independent `Package.swift`.

### Multi-Purpose

This means one repository can serve as both a Kotlin Multiplatform Package and a Native Swift Package, rather than using interpolation and writing native iOS sourceset in Kotlin.

<p align="center">
  <img src="docs/media/diagram.png" alt="RadioPlayer Diagram" width="512">
</p>

## iOS Package

By leveraging the native Kotlin SwiftPM import for importing Swift code into the Kotlin Multiplatform Mobile shared module, RadioPlayer is implemented as a native Swift package.

The iOS component can be developed and maintained independently using the Swift Package Manager, making it easier to manage native iOS code.

It can serve as a native iOS component:

![Swift Package](docs/media/swift-package.png)

## Android Package

On Android, it utilizes ExoPlayer, a powerful and flexible media player.

![Android Package](docs/media/android-package.png)
