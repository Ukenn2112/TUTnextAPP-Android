package com.meikenn.tama.domain.model

data class PrintSettings(
    val plex: PlexType = PlexType.SIMPLEX,
    val nUp: NUpType = NUpType.NONE,
    val startPage: Int = 1,
    val pin: String? = null
)

enum class PlexType(val apiValue: String, val displayName: String) {
    SIMPLEX("simplex", "片面"),
    DUPLEX("duplex", "両面 (長辺とじ)"),
    TUMBLE("tumble", "両面 (短辺とじ)")
}

enum class NUpType(val apiValue: String, val displayName: String) {
    NONE("1", "しない"),
    TWO("2", "2 アップ"),
    FOUR("4", "4 アップ")
}
