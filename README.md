# Auto Wallpaper

[![Android CI/CD](https://github.com/neuos/AutoWallpaper/actions/workflows/android.yml/badge.svg)](https://github.com/neuos/AutoWallpaper/actions/workflows/android.yml)

An Android application that automatically updates your wallpaper.

[**Download Latest APK**](https://github.com/neuos/AutoWallpaper/actions/workflows/android.yml?query=is%3Asuccess+branch%3Amain)

## Screenshots

|                                                     Main Screen                                                      |                                                               Settings                                                               |
|:--------------------------------------------------------------------------------------------------------------------:|:------------------------------------------------------------------------------------------------------------------------------------:|
| ![Main Screen](app/src/test/screenshots/eu.neuhuber.autowallpaper.ui.WallpaperScreenshotTest.snapshotMainScreen.png) | ![Settings Screen](app/src/test/screenshots/eu.neuhuber.autowallpaper.ui.WallpaperScreenshotTest.snapshotMainScreenWithSettings.png) |

## Features

- **Automated Updates**: Change your wallpaper daily or hourly.
- **Multiple Sources**: Support for Bing Wallpaper of the Day and Picsum Photos.
- **Material 3 UI**: Built with Jetpack Compose and modern Material Design principles.

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
