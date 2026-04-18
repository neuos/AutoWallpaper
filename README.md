# Auto Wallpaper

[![Android CI/CD](https://img.shields.io/github/actions/workflow/status/neuos/AutoWallpaper/android.yml?branch=main&label=Android%20CI%2FCD)](https://github.com/neuos/AutoWallpaper/actions/workflows/android.yml)
[![Download APK](https://img.shields.io/github/v/release/neuos/AutoWallpaper?label=Download%20APK&color=green&logo=android&logoColor=white)](https://github.com/neuos/AutoWallpaper/releases/latest)

An Android application that automatically updates your wallpaper.

## Features

- **Automated Updates**: Change your wallpaper daily or hourly.
- **Multiple Sources**: Support for Bing Wallpaper of the Day and Picsum Photos.
- **Material 3 UI**: Built with Jetpack Compose and modern Material Design principles.

## Screenshots

<details>
<summary>Show Screenshots</summary>

|                                                     Main Screen                                                      |                                                               Settings                                                               |
|:--------------------------------------------------------------------------------------------------------------------:|:------------------------------------------------------------------------------------------------------------------------------------:|
| ![Main Screen](app/src/test/screenshots/eu.neuhuber.autowallpaper.ui.WallpaperScreenshotTest.snapshotMainScreen.png) | ![Settings Screen](app/src/test/screenshots/eu.neuhuber.autowallpaper.ui.WallpaperScreenshotTest.snapshotMainScreenWithSettings.png) |

</details>

## Technical Stack

- **UI**: Jetpack Compose
- **Dependency Injection**: Koin
- **Background Tasks**: WorkManager
- **Persistence**: DataStore (Preferences)
- **Logging**: Timber
- **Architecture**: MVVM with Clean Architecture principles

## Versioning

- **Automated Versioning**: `MAJOR.MINOR.PATCH`
    - **Major**: Manually incremented in `build.gradle.kts`.
    - **Minor**: Automatically calculated as the number of commits since the last major change.
    - **Patch**: The GitHub Actions run number.
