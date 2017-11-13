#!/bin/bash

set -e
set -o pipefail
trap 'echo ERROR' ERR

WAR="e621-resteemit"

cd "$(dirname "$0")"
z="$(pwd)"
#gradle clean build
gradle build
cd build/libs
#unpack the war
rm -rf "$WAR"
unzip "$WAR".war -d "$WAR"
#remove servlet stuff
rm -rf "$WAR"/META-INF
rm -rf "$WAR"/WEB-INF

rsync -arv --human-readable --progress --delete-after "$WAR/" "muksihs@muksihs.com:/var/www/html/$WAR/"

xdg-open "http://muksihs.com/$WAR/"

echo "DONE."

