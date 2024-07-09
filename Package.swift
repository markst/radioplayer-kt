// swift-tools-version: 5.7
// The swift-tools-version declares the minimum version of Swift required to build this package.

import PackageDescription

let package = Package(
    name: "RadioPlayer",
    platforms: [.iOS(.v13)],
    products: [
        .library(
            name: "RadioPlayer",
            targets: ["RadioPlayer"]
        )
    ],
    targets: [
        .target(
            name: "RadioPlayer",
            path: "./radioplayer/native/RadioPlayer"
        )
    ]
)
