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

| <img src="https://raw.githubusercontent.com/SapuSeven/BetterUntis/master/fastlane/metadata/android/en-US/images/phoneScreenshots/1.jpg" alt="Screenshot" /> | <img src="https://raw.githubusercontent.com/SapuSeven/BetterUntis/master/fastlane/metadata/android/en-US/images/phoneScreenshots/2.jpg" alt="Screenshot" /> | <img src="https://raw.githubusercontent.com/SapuSeven/BetterUntis/master/fastlane/metadata/android/en-US/images/phoneScreenshots/3.jpg" alt="Screenshot" /> | <img src="https://raw.githubusercontent.com/SapuSeven/BetterUntis/master/fastlane/metadata/android/en-US/images/phoneScreenshots/4.jpg" alt="Screenshot" /> |
| --- | --- | --- | --- |

## Development notes
My [original version](https://github.com/SapuSeven/BetterUntis-Legacy) of BetterUntis had many design and performance flaws.
As a result, development became increasingly more difficult.
I came to the conclusion that it was best to scrap the project and start over from scratch.

Although I reused some parts of the original code, my plan was to entirely switch to Kotlin.
Kotlin has many features and libraries that immensely help to communicate with the Untis API and process the timetable data.

Another major change is the use of a custom WeekView (based on [Till Hellmund](https://github.com/thellmund)'s fork of [Android-Week-View](https://github.com/alamkanak/Android-Week-View)) for the timetable display. This also improved performance by a lot.

## New features
- Select your school by name or ID, no URL needed
- Login by optionally using your password instead of app key
- Zoomable timetable view
- Improved overall design
- Improved timetable selection dialog
- Faster RoomFinder
- Near instantaneous timetable loading
- Lag-free timetable scrolling
- Flexible timegrid allows to display hours outside the regular timetable (like consultation times with teachers)
- Support for multiple accounts
- Support for using a custom proxy server for increased privacy
- Info Center for viewing events, contact hours and own absences.

## Missing features (TODO)
- ~~No support for teacher-specific features (like editing homeworks or class management)~~ - _This is currently being worked on. View progress in the [absence-check](https://github.com/SapuSeven/BetterUntis/tree/absence-check) branch._
- Almost no unit and integration tests

## Available languages
- English
- German

_Versions since 3.3.0 will also include:_
- Chinese (Simplified)
- French
- Norwegian Bokmål

## Project Git Structure
I established a simple system to manage this Git repository.
Basically, there are two main branches: **master** and **develop**. They both are permanent and can't be deleted.

### Branch: master
This branch always and only contains the latest release version. This includes alpha/beta releases.

### Branch: develop
This branch contains the current development version. Small changes and fixes can be committed directly to this branch.

When it reaches a state ready to release, it can be merged into the **master**-branch and a new release can be published.

### Other branches
Especially bigger features which require multiple commits should branch off **develop** and merge back into it. These should be named in a way to describe the feature as clearly as possible.

These branches have a limited lifetime. After the last merge back into **develop**, they should be deleted if no longer needed.

## Contributing

### Translating into your language

Translating BetterUntis is very easy.
Just sign up for an account at [weblate.org](https://hosted.weblate.org/accounts/register/).
After you are logged in, go to the [BetterUntis translation project](https://hosted.weblate.org/projects/betteruntis/translations/). 

There you can click the “Start new translation“ button if your language does not exist.
If your language already exists, you can directly correct possible mistakes.

Your translation will be included in the next release of BetterUntis.

### Implementing new features / Fixing bugs

Anything that requires you to perform code changes should be done on the **develop** branch.
Pull requests should always be based on this branch, except for larger, experimental or incomplete features.
These can be on their own, new feature branch.

_Please **do not** submit pull requests that merge into the master branch, except for changes that **only** affect the repository on GitHub (e.g. README.md changes)._
