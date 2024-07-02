#!/bin/sh
mkdir robolectric-deps
wget -nc https://repo1.maven.org/maven2/org/robolectric/android-all/10-robolectric-5803371/android-all-10-robolectric-5803371.jar -P robolectric-deps
wget -nc https://repo1.maven.org/maven2/org/robolectric/android-all/8.1.0-robolectric-4611349/android-all-8.1.0-robolectric-4611349.jar -P robolectric-deps
wget -nc https://repo1.maven.org/maven2/org/robolectric/android-all-instrumented/8.1.0-robolectric-4611349-i3/android-all-instrumented-8.1.0-robolectric-4611349-i3.jar -P robolectric-deps
wget -nc https://repo1.maven.org/maven2/org/robolectric/android-all/8.0.0_r4-robolectric-r1/android-all-8.0.0_r4-robolectric-r1.jar -P robolectric-deps

wget -nc https://repo1.maven.org/maven2/org/robolectric/android-all/6.0.1_r3-robolectric-r1/android-all-6.0.1_r3-robolectric-r1.jar -P robolectric-deps
wget -nc https://repo1.maven.org/maven2/org/robolectric/android-all/7.0.0_r1-robolectric-r1/android-all-7.0.0_r1-robolectric-r1.jar -P robolectric-deps
wget -nc https://repo1.maven.org/maven2/org/robolectric/android-all/7.1.0_r7-robolectric-r1/android-all-7.1.0_r7-robolectric-r1.jar -P robolectric-deps
wget -nc https://repo1.maven.org/maven2/org/robolectric/android-all/9-robolectric-4913185-2/android-all-9-robolectric-4913185-2.jar -P robolectric-deps
wget -nc https://repo1.maven.org/maven2/org/robolectric/android-all/11-robolectric-6757853/android-all-11-robolectric-6757853.jar -P robolectric-deps

cp robolectric-deps.properties library/src/test/resources
cp robolectric-deps.properties utils/src/test/resources
cp robolectric-deps.properties sample/src/test/resources