#/bin/sh
echo Releasing with version: $1
git tag -a $1 -m $1
git push --tags
./gradlew bintrayUpload