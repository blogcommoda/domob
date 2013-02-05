#!/bin/bash

originalSvg="./logo.svg"
outputImage="domob_icon.png"
# Paths corresponding to LDPI, MDPI, HDPI, XHDPI, and the developer console
outPaths=("../res/drawable-ldpi/$outputImage" \
          "../res/drawable-mdpi/$outputImage" \
          "../res/drawable-hdpi/$outputImage" \
          "../res/drawable-xhdpi/$outputImage" \
          "./dev-$outputImage")
# DPIs corresponding to LDPI, MDPI, HDPI, XHDPI, and the developer console
dpiWidths=(36 48 72 96 512)

for ((i=0; i < ${#outPaths[@]}; i++))
do
  inkscape --export-png=${outPaths[$i]} \
           --export-width=${dpiWidths[$i]} \
           --export-background-opacity=0 \
           --without-gui \
           $originalSvg
done

