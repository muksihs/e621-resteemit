#!/bin/bash

set -e
set -o pipefail
trap 'echo ERROR' ERR

WAR="e621-resteemit"

cd "$(dirname "$0")"
z="$(pwd)"

#automatic version bump
version="$(date +yyyyMMdd)"
sed -i "#<set-configuration-property name=\"version\" value=\"........\"/>#<set-configuration-property name="version" value="$version"/>#" ./src/main/resources/muksihs/e621/resteemit/e621resteemit.gwt.xml
sed -i "#version = '........'#version = '$version'#" build.gradle
git add ./src/main/resources/muksihs/e621/resteemit/e621resteemit.gwt.xml || true
git add build.gradle || true
git commit -a -m "autocommit on build" || true

#build
gradle clean
gradle build
cd build/libs
#unpack the war
rm -rf "$WAR"
unzip "$WAR".war -d "$WAR"
#remove servlet stuff
rm -rf "$WAR"/META-INF
rm -rf "$WAR"/WEB-INF

#publish
rsync -arv --human-readable --progress --delete-after "$WAR/" "muksihs@muksihs.com:/var/www/html/$WAR/"

xdg-open "http://muksihs.com/$WAR/"

echo "DONE."

