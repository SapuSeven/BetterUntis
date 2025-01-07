![App Icon](https://raw.githubusercontent.com/SapuSeven/BetterUntis/master/app/src/main/res/mipmap-xxxhdpi/ic_launcher.png)
# BetterUntis
An alternative mobile client for the Untis timetable system.

<a href="https://circleci.com/gh/SapuSeven/BetterUntis"><img src="https://img.shields.io/circleci/build/gh/SapuSeven/BetterUntis?style=for-the-badge" alt="CircleCI"></a>
<a href="https://hosted.weblate.org/engage/betteruntis/?utm_source=widget"><img src="https://img.shields.io/badge/dynamic/xml?color=green&label=localized&query=%2F%2F%2A%5Blocal-name%28%29%3D%27text%27%5D%5Blast%28%29%5D%2Ftext%28%29&url=https%3A%2F%2Fhosted.weblate.org%2Fwidgets%2Fbetteruntis%2F-%2Fsvg-badge.svg&style=for-the-badge" alt="Translation status" width=147.5 height=28/></a>
<a href="https://matrix.to/#/#github-betteruntis:sapuseven.com"><img src="https://img.shields.io/badge/chat-on matrix-blueviolet?style=for-the-badge" alt="Matrix"></a>
<a href="https://ko-fi.com/sapuseven"><img src="https://img.shields.io/badge/Support-On Ko--fi-%2313C3FF?style=for-the-badge" alt="Support on Ko-fi"/></a>

<a href="https://f-droid.org/packages/com.sapuseven.untis"><img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png" alt="Get it on F-Droid" height="80"></a>
<a href="https://play.google.com/store/apps/details?id=com.sapuseven.untis&utm_source=github&utm_campaign=badge"><img alt="Get it on Google Play" src="https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png" height="80"/></a>

You can also download the latest automated build [from my website](https://sapuseven.com/app/BetterUntis).

## Screenshots

| <img src="https://raw.githubusercontent.com/SapuSeven/BetterUntis/master/fastlane/metadata/android/en-US/images/phoneScreenshots/1.png" alt="Screenshot 1" /> | <img src="https://raw.githubusercontent.com/SapuSeven/BetterUntis/master/fastlane/metadata/android/en-US/images/phoneScreenshots/2.png" alt="Screenshot 2" /> | <img src="https://raw.githubusercontent.com/SapuSeven/BetterUntis/master/fastlane/metadata/android/en-US/images/phoneScreenshots/3.png" alt="Screenshot 3" /> | <img src="https://raw.githubusercontent.com/SapuSeven/BetterUntis/master/fastlane/metadata/android/en-US/images/phoneScreenshots/4.png" alt="Screenshot 4" /> |
|---------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------|

## Development history

### v3.x
The [original version](https://github.com/SapuSeven/BetterUntis-Legacy) of BetterUntis had many design and performance flaws.
Since development became increasingly more difficult, the project was scrapepd and I started over from scratch.

Although some parts of the original code were reused, the codebase was entirely switched to Kotlin.
Kotlin has many features and libraries that immensely help to communicate with the Untis API and process the timetable data.

Another major change is the use of a custom WeekView (based on [Till Hellmund](https://github.com/thellmund)'s fork of [Android-Week-View](https://github.com/alamkanak/Android-Week-View)) for the timetable display.
This also improved performance by a lot.

### v4.x
This major version updated the WeekView component again to be based entirely on Jetpack Compose.
Since the rest of the app was already built on Compose, this made it easier to develop and improve the WeekView component.

### v5.x (WIP)
This version introduces a complete architecture overhaul to follow the latest Android Jetpack guidelines.
Additionally, several components (like Untis API Client, Compose Preferences, ...) were separated to their own module
to potentially reuse them in other projects.

As this changes virtually every part of the app, here's a list tracking all migrated components:

- [ ] Timetable view
  - [x] WeekView
  - [x] Element picker
  - [x] Caching
  - [x] Element info
    - [x] Basic view
    - [x] Teacher features
- [x] Preferences
  - [x] Theme management
- [ ] Room finder
- [ ] Info center
- [ ] Widget
- [ ] Notifications

Package structure overview:

- `activities`: Entry points for the app
- `annotations`: Custom annotations
- `components`: Reusable components for DI - _needs evaultion_
- `data`: Data layer
  - `cache`: Caching-related code
  - `database`: Room database and DAOs
  - `model`: (Not used yet) Data models
  - `repository`: Repositories for accessing data in a domain-specific way
  - `settings`: _To be removed_
  - `timetable`: _To be removed_
- `helpers`: _To be removed_
- `mappers`: Mapping between different data models
- `models`: Old data models, _to be removed_
- `modules`: Dagger Hilt modules for DI
- `preferences`: _To be removed_
- `receivers`: Broadcast receivers
- `scope`: Custom DI scopes - _needs evaluation_
- `services`: _To be removed_
- `ui`: UI layer
  - `activities`: Contains every main page in a sub-package. _to be renamed ("pages"?)_
  - `animations`: Custom animation implementations
  - `common`: Shared Compose UI components
  - `datepicker`: Date picker implementation
  - `dialogs`: Custom dialogs
  - `functional`: Functional Compose components - _could be merged with `common`_
  - `models`: _To be removed_
  - `navigation`: App navigation
  - `preferences`: Custom ProtoStore preference implementations
  - `theme`: Theme-related code
  - `weekview`: WeekView implementation
  - `widgets`: Glance Compose widget components - _needs evaluation_
- `widgets`: Existing widget implementation, _consolidate with above_
- `workers`: WorkManager workers and background tasks

It may be viable to switch to a feature-based package structure, but this needs further evaluation.
In any case, any larger changes will need to wait until this version is released and old code is removed.

## New features
- Search for your school by name or ID, no URL needed
- Login by using your password or app key
- Zoomable timetable view
- Improved overall design
- Improved timetable selection dialog
- Faster RoomFinder
- Faster timetable loading
- Lag-free timetable scrolling
- Flexible timegrid allows to display hours outside the regular timetable (like consultation times with teachers)
- Support for multiple accounts/profiles
- Support for using a custom proxy server for increased privacy
- Info Center for viewing events, contact hours and own absences
- Class management features for teachers (Absence checking, lesson topic editing, ...)

## Available languages
- English
- German

_Since v3.3.0:_
- Chinese (Simplified)
- French
- Norwegian Bokmål

_This list may not be up-to-date. Please look at "Translating" below to check the current translation status._

## Project Git Structure
I established a simple system to manage this Git repository.
Basically, there are two main branches: **master** and **develop**. They both are permanent and cannot be deleted.

### Branch: master
This branch always and only contains the latest release version. This includes alpha/beta releases.

### Branch: develop
This branch contains the current development version. Small changes and fixes can be committed directly to this branch.

When it reaches a state ready to release, it can be merged into the **master**-branch and a new release can be published.

### Other branches
Especially bigger features which require multiple commits should branch off **develop** and merge back into it. These should be named in a way to describe the feature as clearly as possible.

These branches have a limited lifetime. After the last merge back into **develop**, they should be deleted if no longer needed.

## Contributing

Your help is greatly appreciated, but first please read the following sections to prevent common mistakes:

### Translating into another language

If you haven't already, sign up for an account at [weblate.org](https://hosted.weblate.org/accounts/register/) .
You can then navigate to the [BetterUntis translation project](https://hosted.weblate.org/projects/betteruntis/translations/). 

Select your desired language (or click the “Start new translation“ button at the bottom) and look at "Strings status".
All categories marked in red are areas where translations are missing or potentially incorrect. Click on them to start translating.

Your translation will then be automatically included in the next release of BetterUntis.

### Implementing new features / Fixing bugs

Anything that requires you to perform code changes should be done on the **develop** branch.
Pull requests should always be based on this branch, except for larger, experimental or incomplete features.
These can be on their own, new feature branch.

_Please **do not** submit pull requests that merge into the master branch, except for changes that **only** affect the repository on GitHub (e.g. README.md changes)._
