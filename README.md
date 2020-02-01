# BetterUntis
An alternative mobile client for the Untis timetable system.

<a href="https://circleci.com/gh/SapuSeven/BetterUntis"><img src="https://img.shields.io/circleci/build/gh/SapuSeven/BetterUntis?style=for-the-badge" alt="CircleCI"></a>
<a href="https://hosted.weblate.org/engage/betteruntis/?utm_source=widget"><img src="https://hosted.weblate.org/widgets/betteruntis/-/svg-badge.svg" alt="Translation status" width=147.5 height=28/></a>
<a href="https://gitter.im/SapuSeven/BetterUntis"><img src="https://img.shields.io/gitter/room/SapuSeven/BetterUntis?color=blueviolet&style=for-the-badge" alt="Gitter"></a>

You can download the latest automated debug build [from my website](https://sapuseven.com/app/BetterUntis).

<a href="https://f-droid.org/packages/com.sapuseven.untis"><img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png" alt="Get it on F-Droid" height="80"></a>
<a href="https://play.google.com/store/apps/details?id=com.sapuseven.untis&utm_source=github&utm_campaign=badge"><img alt="Get it on Google Play" src="https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png" height="80"/></a>


## Development history
It was more and more obvious that my [original version](https://github.com/SapuSeven/BetterUntis-Legacy) of BetterUntis had many design and performance flaws.
As a result, development became increasingly more difficult.
So I came to the conclusion to scrap the project and start over from scratch.

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
- No support for teacher-specific features (like editing homeworks or class management)
- Almost no unit and integration tests

## Available languages
- English
- German

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

Translating BetterUntis is very easy. Just sign up for an account at weblate.org.
After you are logged in, go to the BetterUntis translation project. 

There you can click the “Start new translation“ button if your language does not exist.
If your language already exists, you can directly correct possible mistakes.

Your translation will be included in the next release of BetterUntis.

You will find all details on [weblate.org](https://hosted.weblate.org/engage/betteruntis/).
