# BetterUntis
An alternative mobile client for the Untis timetable system.

## Development
It was more and more obvious that my [original version](https://github.com/SapuSeven/BetterUntis-Legacy) of BetterUntis had many design and performance flaws.
As a result, development became increasingly more difficult.
So I came to the conclusion to scrap the project and start over from scratch.

Although I reused some parts of the original code, my plan is to entirely switch to Kotlin.
Kotlin has many features and libraries that immensly help to communicate with the Untis API and process the timetable data.

Another major change is the use of a custom WeekView (based on [Till Hellmund](https://github.com/thellmund)'s fork of [Android-Week-View](https://github.com/alamkanak/Android-Week-View)) for the timetable display. This also improved performance by a lot.

## New features
- Select your school by searing the name instead of providing an URL
- Login by optionally using your password instead of app key
- Zoomable timetable view
- Improved overall design
- Improved timetable selection dialog
- Faster RoomFinder
- Near instantaneous timetable loading
- Lag-free timetable scrolling
- Flexible timegrid allows to display hours outside the regular timetable (like consultation times with teachers)

## Missing features / Ways to contribute

- No holiday or free day indicators
- No translations, as of now the app is only available in English
- No notifications
- No in-timetable room availability indicator
- No custom personal timetable
- No account settings
- No support for multiple accounts _(although I wrote timetable-specific code in a way to allow relatively easy implementation of this)_
- No 'last updated'-indicator
