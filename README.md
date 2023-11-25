![App Icon](https://raw.githubusercontent.com/SapuSeven/BetterUntis/master/app/src/main/res/mipmap-xxxhdpi/ic_launcher.png)

# BetterUntis
An alternative mobile client for the Untis timetable system.

<a href="https://circleci.com/gh/SapuSeven/BetterUntis"><img src="https://img.shields.io/circleci/build/gh/SapuSeven/BetterUntis?style=for-the-badge" alt="CircleCI"></a>
<a href="https://hosted.weblate.org/engage/betteruntis/?utm_source=widget"><img src="https://img.shields.io/badge/dynamic/xml?color=green&label=localized&query=%2F%2F%2A%5Blocal-name%28%29%3D%27text%27%5D%5Blast%28%29%5D%2Ftext%28%29&url=https%3A%2F%2Fhosted.weblate.org%2Fwidgets%2Fbetteruntis%2F-%2Fsvg-badge.svg&style=for-the-badge" alt="Translation status"></a>
<a href="https://matrix.to/#/#github-betteruntis:sapuseven.com"><img src="https://img.shields.io/badge/chat-on matrix-blueviolet?style=for-the-badge" alt="Matrix"></a>
<a href="https://ko-fi.com/sapuseven"><img src="https://img.shields.io/badge/Support-On Ko--fi-%2313C3FF?style=for-the-badge" alt="Support on Ko-fi"></a>

<a href="https://f-droid.org/packages/com.sapuseven.untis"><img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png" alt="Get it on F-Droid" height="80"></a>
<a href="https://play.google.com/store/apps/details?id=com.sapuseven.untis&utm_source=github&utm_campaign=badge"><img alt="Get it on Google Play" src="https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png" height="80"/></a>

Download the latest automated build [from the developer's website](https://sapuseven.com/app/BetterUntis).

## Screenshots

| ![Screenshot 1](https://raw.githubusercontent.com/SapuSeven/BetterUntis/master/fastlane/metadata/android/en-US/images/phoneScreenshots/1.png) | ![Screenshot 2](https://raw.githubusercontent.com/SapuSeven/BetterUntis/master/fastlane/metadata/android/en-US/images/phoneScreenshots/2.png) | ![Screenshot 3](https://raw.githubusercontent.com/SapuSeven/BetterUntis/master/fastlane/metadata/android/en-US/images/phoneScreenshots/3.png) | ![Screenshot 4](https://raw.githubusercontent.com/SapuSeven/BetterUntis/master/fastlane/metadata/android/en-US/images/phoneScreenshots/4.png) |
|---|---|---|---|

## Development Notes (v3.x.x)
The [original version](https://github.com/SapuSeven/BetterUntis-Legacy) of BetterUntis faced several design and performance challenges. These led to a decision to rebuild the project from the ground up.

Key Improvements:
- Transition to Kotlin, enhancing API communication and data processing.
- Adoption of a custom WeekView (based on [Till Hellmund](https://github.com/thellmund)'s fork of [Android-Week-View](https://github.com/alamkanak/Android-Week-View)), boosting performance.

## New Features
- Easy school search by name or ID.
- Password or app key login.
- Zoomable timetable view.
- Enhanced design and usability.
- Improved timetable selection dialog.
- Faster RoomFinder and loading times.
- Smooth timetable scrolling.
- Flexible timegrid for extended hour display.
- Multiple account/profile support.
- Custom proxy server option for privacy.
- Info Center for events, contact hours, and absences.
- Class management tools for teachers.

## Available Languages
[![Translation status](https://hosted.weblate.org/widget/betteruntis/translations/multi-auto.svg)](https://hosted.weblate.org/engage/betteruntis/)

### Project Git Structure
>[!Note]
The BetterUntis repository is managed with a clear and efficient branching strategy to ensure smooth development and release processes. Here's an overview:
>
> - **Branch: master**  
    The master branch is the backbone of the repository, representing the stable, public face of BetterUntis. It contains the latest released 
    version of the application, including all stable alpha and beta versions. Updates to this branch are typically the result of a culmination 
    of tested and approved changes from the development branch.
>
> - **Branch: develop**  
    This is where the magic happens. The develop branch serves as the primary workspace for ongoing development. It is the breeding ground for 
    new features, enhancements, and bug fixes. Small, incremental changes are directly committed to this branch. Once these changes are vetted 
    and ready for public release, they are merged into the master branch, marking the next step in BetterUntis' evolution.
>
> - **Feature-Specific Branches**  
    For larger updates, complex features, or experiments that require a series of commits and more significant development work, separate 
    branches are created from develop. These branches are named descriptively to reflect the feature or improvement they are intended to 
    address. Upon completion, they are merged back into the develop branch. Post-merge, these feature-specific branches are deleted if they 
    are no longer needed, keeping the repository clean and organized.

## Contributing

### Translating into Another Language
- Sign up at [weblate.org](https://hosted.weblate.org/accounts/register/).
- Visit the [BetterUntis translation project](https://hosted.weblate.org/projects/betteruntis/translations/).
- Select a language to start translating.
- Your contributions will be included in the next release.

### Implementing New Features / Fixing Bugs
>[!Important]
Your contributions in the form of new features and bug fixes are invaluable to the BetterUntis project. However, to ensure a smooth integration of your work, please adhere to the following guidelines:
>
> - **Development Focus**  
    All code changes, whether they are new features or bug fixes, should primarily be based on and made to the **develop** branch. This 
    ensures 
    that your contributions are incorporated into the ongoing development process and tested thoroughly before being included in a public 
    release.
>
> - **Pull Requests**  
    When you're ready to submit your work, create a pull request targeting the **develop** branch. This is crucial for maintaining the 
    integrity of the master branch as a stable, release-ready version of BetterUntis. 
>
> - **Feature Branches for Major Changes**  
    If you are working on a larger feature or an experimental update that is not yet complete or requires extensive testing, it is advisable 
    to use a separate feature branch. This approach allows for isolated development and testing of major changes without affecting the overall 
    stability of the develop branch.
>
> - **Avoid Direct Master Branch PRs**  
    Please refrain from submitting pull requests that merge directly into the master branch. The only exception to this rule is for changes 
    that are exclusively related to the repository's documentation or non-code assets on GitHub, such as updates to the README.md file.
