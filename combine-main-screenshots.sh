#!/bin/sh
cd screenshots

files=(timetable-*.png)
total=${#files[@]}
i=0

mkdir split
for f in "${files[@]}"; do
  magick convert -crop $(($total/2))x2@ $f split.png
  mv split-$i.png split/
  rm split-*.png
  i=$(( i + 1 ))
done

magick montage $(ls -1v split/*.png) -tile $(($total/2))x2 -geometry +0+0 split.png
rm -r split
