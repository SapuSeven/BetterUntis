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

# Create split image for main activity theme
SPLIT_IMAGE_1=activity-main-4-green.png
SPLIT_IMAGE_2=activity-main-dark-6-indigo.png
SPLIT_IMAGE_DST=activity-main-theme.png
SPLIT_RATIO=0.35
 # 0.0 = / (Diagonal from bottom-left)
 # 0.5 = | (Completely Vertical)
 # 1.0 = \ (Diagonal from top-left)

magick $SPLIT_IMAGE_1 \
 \( +clone +level-colors white -fill black -draw "polygon %[fx:int(w*$SPLIT_RATIO)],%[fx:h] %[fx:w],%[fx:h] %[fx:w],0 %[fx:int(w*$((1-SPLIT_RATIO)))],0" \) \
 -alpha off -compose copyopacity -composite \
 $SPLIT_IMAGE_2 +swap \
 -compose over -composite \
 $SPLIT_IMAGE_DST

# Create showcase images with screenshots and text
SHADOW_OPACITY=50
SHADOW_RADIUS=50
BORDER_RADIUS=50
SCREENSHOT_SCALE=70 # in percent
SHOWCASE_TEXT_FONT="../fonts/Poppins-Light.ttf"
SHOWCASE_TEXT=(
  "activity-main-1-red.png:View your timetable"
  "activity-main-theme.png:Customize the look\nof your timetable"
  "activity-roomfinder.png:Find free rooms"
  "activity-infocenter.png:Everything at a glance"
)

for t in $SHOWCASE_TEXT; do
  magick -size 1080x1920 xc:white `# Create background`\
    \( \
      \( \
        \( \
          ${t%:*} `# Src image`\
          \( +clone +level-colors black -fill white -draw "roundrectangle 0,0,%[fx:w],%[fx:h],${BORDER_RADIUS},${BORDER_RADIUS}" \) `# Create an alpha mask for rounded corners`\
          -alpha off -compose copy-opacity -composite `# Apply alpha mask`\
        \) `# Create src image with rounded corners`\
        \( +clone -background black -shadow ${SHADOW_OPACITY}x${SHADOW_RADIUS} \) `# Create drop shadow`\
        +swap -background none -compose src-over -layers merge +repage `# Combine shadow with image`\
      \) \
      -resize 70% `# Scale down`\
    \) \
    -gravity South -geometry +0-$((2*SHADOW_RADIUS*(SCREENSHOT_SCALE/100.0) + BORDER_RADIUS*(SCREENSHOT_SCALE/100.0))) -composite `# Place the screenshot with shadow over the background`\
    \( -gravity Center -size $((1080*0.8))x$((1920*0.2)) -fill black -font ${SHOWCASE_TEXT_FONT} -pointsize 48 label:"${t#*:}" \) `# Create text`\
    -gravity North -composite `# Place the screenshot with shadow over the background`\
    showcase-${t%:*}
done
