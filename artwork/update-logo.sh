#!/bin/bash

originalSvg="./logo.svg"
outputImage="domob_icon.png"
lDpiWidth=36
mDpiWidth=48
hDpiWidth=72
xhDpiWidth=96

convert -background none $originalSvg -resize $lDpiWidth ../res/drawable-ldpi/$outputImage
convert -background none $originalSvg -resize $mDpiWidth ../res/drawable-mdpi/$outputImage
convert -background none $originalSvg -resize $hDpiWidth ../res/drawable-hdpi/$outputImage
convert -background none $originalSvg -resize $xhDpiWidth ../res/drawable-xhdpi/$outputImage
