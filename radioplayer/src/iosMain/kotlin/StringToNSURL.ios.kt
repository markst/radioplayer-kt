package dev.markturnip.radioplayer

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSURL

@OptIn(ExperimentalForeignApi::class)
internal fun String.toNSURL(): NSURL = when {
    startsWith("http://", ignoreCase = true) || startsWith("https://", ignoreCase = true) ->
        NSURL(string = this)
    else ->
        NSURL(fileURLWithPath = this)
}
