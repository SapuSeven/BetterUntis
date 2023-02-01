#!/bin/sh
kill $(lsof -t app/build/outputs/androidTest-results/connected/*/.fuse_hidden*)

#./gradlew -PtestInstrumentationRunnerArguments.package=com.sapuseven.untis.screenshots connectedAndroidTest

[ -d screenshots/. ] || mkdir screenshots
for f in $(adb exec-out run-as com.sapuseven.untis.debug ls "/data/data/com.sapuseven.untis.debug/files/screenshots"); do
  adb exec-out run-as com.sapuseven.untis.debug cat "/data/data/com.sapuseven.untis.debug/files/screenshots/$f" > "screenshots/$f"
done
