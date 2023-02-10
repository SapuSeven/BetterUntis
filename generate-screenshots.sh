#!/bin/sh
# Workaround (weird bug that locks a file and prevents any other tests from being run`)
kill $(lsof -t app/build/outputs/androidTest-results/connected/*/.fuse_hidden*)

SCREENSHOTS_BASE_PATH=/data/data/com.sapuseven.untis.debug/files/screenshots

# Delete all existing screenshots on device and locally
adb exec-out run-as com.sapuseven.untis.debug rm -r $SCREENSHOTS_BASE_PATH
rm -r screenshots/*

# Run Android tests to generate screenshots
./gradlew -PtestInstrumentationRunnerArguments.package=com.sapuseven.untis.screenshots connectedAndroidTest

# Copy screenshots from device to local
[ -d screenshots/. ] || mkdir screenshots
for f in $(adb exec-out run-as com.sapuseven.untis.debug ls $SCREENSHOTS_BASE_PATH); do
  adb exec-out run-as com.sapuseven.untis.debug cat "$SCREENSHOTS_BASE_PATH/$f" > "screenshots/$f"
done

# Do some image magick to make the screenshots look pretty
cd screenshots

SPLIT_IMAGE_1=activity-main-1-red.png
SPLIT_IMAGE_2=activity-main-dark-5-blue.png
SPLIT_IMAGE_DST=main-activity-theme.png
SPLIT_RATIO=0.35

linespec=$(convert $SPLIT_IMAGE_1 -format "%[fx:int(w*$SPLIT_RATIO)],%[fx:h] %[fx:w],%[fx:h] %[fx:w],0 %[fx:int(w*$((1-SPLIT_RATIO)))],0" info:)
magick $SPLIT_IMAGE_1 \
 \( +clone +level-colors white -fill black -draw "polygon $linespec" \)  -alpha off \
 -compose copyopacity -composite \
 $SPLIT_IMAGE_2 +swap \
 -compose over -composite \
 $SPLIT_IMAGE_DST
