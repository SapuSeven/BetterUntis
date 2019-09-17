# BetterUntis
An alternative mobile client for the Untis timetable system.

## Development history
It was more and more obvious that my [original version](https://github.com/SapuSeven/BetterUntis-Legacy) of BetterUntis had many design and performance flaws.
As a result, development became increasingly more difficult.
So I came to the conclusion to scrap the project and start over from scratch.

Although I reused some parts of the original code, my plan is to entirely switch to Kotlin.
Kotlin has many features and libraries that immensly help to communicate with the Untis API and process the timetable data.

Another major change is the use of a custom WeekView (based on [Till Hellmund](https://github.com/thellmund)'s fork of [Android-Week-View](https://github.com/alamkanak/Android-Week-View)) for the timetable display. This also improved performance by a lot.

## New features
- Select your school by searching the name instead of providing a URL
- Login by optionally using your password instead of app key
- Zoomable timetable view
- Improved overall design
- Improved timetable selection dialog
- Faster RoomFinder
- Near instantaneous timetable loading
- Lag-free timetable scrolling
- Flexible timegrid allows to display hours outside the regular timetable (like consultation times with teachers)
- Support for multiple accounts

## Missing features
- No holiday or free day indicators
- No translations, as of now the app is only available in English
- No in-timetable room availability indicator
- No 'last updated'-indicator
- Almost no unit and integration tests

## Project Git Structure
I established a simple system to manage this Git repository.
Basically, there are two main branches: **master** and **develop**. They both are permanent and can't be deleted.

### Branch: master
This branch always and only contains the latest release version. This includes alpha/beta releases.

_Note: As the app hasn't been released yet, this branch doesn't contain any useful code (see below)._

### Branch: develop
This branch contains the current development version. Small changes and fixes can be committed directly to this branch.

When it reaches a state ready to release, it can be merged into the **master**-branch and a new release can be published.

### Other branches
Especially bigger features which require multiple commits should branch off **develop** and merge back into it. These should be named in a way to describe the feature as clearly as possible.

These branches have a limited lifetime. After the last merge back into **develop**, they should be deleted if no longer needed.
