package eu.neuhuber.autowallpaper.model

enum class HomescreenMode(val label: String) {
    NONE("No"),
    SCROLLING("Scrolling"),
    STATIC("Static")
}

enum class LockscreenMode(val label: String) {
    NONE("No"),
    YES("Yes")
}

data class WallpaperSettings(
    val homescreen: HomescreenMode = HomescreenMode.SCROLLING,
    val lockscreen: LockscreenMode = LockscreenMode.YES,
    val provider: String = "Bing",
    val schedule: String = "Daily"
)
