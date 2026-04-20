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

## CI / Automation Setup

### Required repository settings

1. **Settings → General → Pull Requests → Allow auto-merge** — must be enabled so that
   `gh pr merge --auto` can queue a merge once all required checks pass.
2. *(Optional)* **Settings → General → Pull Requests → Automatically delete head branches** —
   deletes the source branch after a merge. The Auto-merge workflow also passes
   `--delete-branch` as a safety net for branches within the same repo.

### `AUTO_MERGE_TOKEN` secret

The Auto-merge workflow uses a dedicated secret called `AUTO_MERGE_TOKEN` instead of the
default `GITHUB_TOKEN` for one important reason:

> **`GITHUB_TOKEN`-triggered merges do not fire other workflows.**
> GitHub prevents recursive workflow loops by not raising `push` events when a workflow
> itself performs the push/merge via `GITHUB_TOKEN`. Using a PAT (Personal Access Token)
> causes the merge commit to be attributed to the token owner and therefore triggers
> downstream workflows (e.g., the Build & Release CI on push to `main`) normally.

**How to create and store `AUTO_MERGE_TOKEN`:**

1. Go to **GitHub → Settings → Developer settings → Personal access tokens**.
2. Create a new **fine-grained PAT** (or classic PAT with `repo` scope) for the account or
   bot that should perform the merge:
    - *Fine-grained*: grant **Read and Write** access to **Pull requests** and **Contents**
      for this repository.
    - *Classic*: `repo` scope is sufficient.
3. Copy the generated token.
4. In this repository go to **Settings → Secrets and variables → Actions → New repository
   secret**, name it `AUTO_MERGE_TOKEN`, and paste the token.
