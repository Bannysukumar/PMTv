<!-- readme-seo: bannysukumar-professional-v4 -->

# PMTv

PMTv is an Android live TV application. The Android package in this repository is `com.smart.pmtv`, with `MainActivity` and `SplashActivity` under `app/src/main/java`. A separate web admin lives in `admin-panel`.

## Overview

The Gradle build uses the Android application plugin and Google services. `logo.jpeg` is in the repository root. `PMATv` is a second public repository with the same Android package name. This README is for `PMTv` only. Nothing was archived or renamed.

## Features

- Android app module `app` with package `com.smart.pmtv`
- `MainActivity` and `SplashActivity`
- Web admin project in `admin-panel`

## Tech Stack

| Technology | Where it shows up |
|---|---|
| Java | `app/src/main/java/com/smart/pmtv` |
| Android Gradle | `build.gradle.kts`, `settings.gradle.kts`, `gradlew` |
| Google services | Android Gradle plugin alias `google.services` |

## Architecture

Android app module `app` plus a web project in `admin-panel`.

## Project Structure

```text
PMTv/
├── app/
├── admin-panel/
├── gradle/
├── build.gradle.kts
├── settings.gradle.kts
└── logo.jpeg
```

## Prerequisites

- Android Studio, or a JDK plus the Gradle wrapper already in the repo

## Installation

```bash
git clone https://github.com/Bannysukumar/PMTv.git
cd PMTv
```

Open the project in Android Studio, or run the Gradle wrapper from this directory.

## Usage

Build and run the `app` module. The admin UI is the separate `admin-panel` project.

## Contributing

Read [CONTRIBUTING.md](CONTRIBUTING.md) before opening a pull request.

## License

Licensed under MIT. See [LICENSE](LICENSE).

## Author

Banny Sukumar

GitHub: https://github.com/Bannysukumar
