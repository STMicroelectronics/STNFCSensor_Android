#! /bin/bash

export PATH=~/Library/Android/sdk/build-tools/28.0.3/:$PATH
versionName=$1

git tag $versionName
mkdir ../$versionName

zip -r ../$versionName/src_$versionName.zip . -x '*.git*' '*build*' '*.gradle/*' '*release*'

./gradlew assembleRelease

cp app/build/outputs/apk/release/app-release-unsigned.apk ../$versionName/$versionName-unsigned.apk

zipalign -v -p 4 ../$versionName/$versionName-unsigned.apk ../$versionName/$versionName-unsigned-aligned.apk

apksigner sign --v1-signing-enabled  --v2-signing-enabled   --ks ../myreleasekey.jks --ks-key-alias MyReleaseKey --ks-pass pass:password --out ../$versionName/$versionName-release.apk --in ../$versionName/$versionName-unsigned-aligned.apk
apksigner verify ../$versionName/$versionName-release.apk
