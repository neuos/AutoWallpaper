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

enum class ScheduleMode(val label: String) {
    NONE("Off"),
    HOURLY("Hourly"),
    DAILY("Daily")
}

data class WallpaperSettings(
    val homescreen: HomescreenMode = HomescreenMode.STATIC,
    val lockscreen: LockscreenMode = LockscreenMode.YES,
    val provider: String = "Bing",
    val schedule: ScheduleMode = ScheduleMode.DAILY
)
