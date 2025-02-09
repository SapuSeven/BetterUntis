#!/usr/bin/env zsh

### Run this script manually from the project dir if you need to generate new screenshots.
### As of now, the screenshots have to be manually copied and uploaded from the newly generated `screenshots` dir.

# Workaround (weird bug that locks a file and prevents any other tests from being run`)
#kill $(lsof -t app/build/outputs/androidTest-results/connected/*/.fuse_hidden*)

SCREENSHOTS_BASE_PATH=/data/data/com.sapuseven.untis.debug/files/screenshots

# Delete all existing screenshots on device and locally
adb exec-out run-as com.sapuseven.untis.debug rm -r $SCREENSHOTS_BASE_PATH
rm -r screenshots/*

# Run Android tests to generate screenshots
./gradlew -PtestInstrumentationRunnerArguments.package=com.sapuseven.untis.screenshots connectedGmsDebugAndroidTest

# Copy screenshots from device to local
[ -d screenshots/. ] || mkdir screenshots
for f in $(adb exec-out run-as com.sapuseven.untis.debug ls $SCREENSHOTS_BASE_PATH); do
  adb exec-out run-as com.sapuseven.untis.debug cat "$SCREENSHOTS_BASE_PATH/$f" > "screenshots/$f"
done
