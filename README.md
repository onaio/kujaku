# Kujaku (Peacock) [![Build Status](https://travis-ci.org/onaio/kujaku.svg?branch=master)](https://travis-ci.org/onaio/kujaku)

A mapping and check-in library for Android using MapBox SDK.

# Table of Contents

* [Setup Instructions](#setup-instructions)
  * [Running the Sample App](#running-sample-app)
  * [Importing the Library](#importing-the-library)
  * [How to publish artifacts](#how-to-publish-artifacts)
* [How to import the library](#how-to-import-the-library)
* [License](#license)
* [Acknowledgements](#acknowledgements)

## Setup Instructions

### Running Sample App

For instructions on how to run the sample app see [these instructions](./sample/README.md).

### Importing the Library

### How to publish artifacts

To publish new versions to the **Bintray/JFrog** account, run:

```
export BINTRAY_USER=johndoe
export BINTRAY_KEY=98sdfkmykeyhere90sdckl
./gradlew clean assembleRelease :utils:bintrayUpload
./gradlew clean assembleRelease :library:bintrayUpload

```

To publish locally:

```
./gradlew clean assembleRelease :utils:publishToMavenLocal
./gradlew clean assembleRelease :library:publishToMavenLocal

```

## How to import the library

To import the library:

1. Add the following snippet to your app module `build.gradle`

```
...


allprojects {
    repositories {
        maven { url "http://dl.bintray.com/ona/kujaku" }
    }
}

```
This adds the bintray repository to your configuration

2. Add the following snippet to your app module `build.gradle`

```
...
android {

... 

dependencies {
    ...
    // Kujaku dependencies
    implementation('io.ona.kujaku:library:0.3.0') {
        transitive = true
        exclude group: 'com.mapbox.mapboxsdk', module: 'mapbox-android-sdk'
        exclude group: 'com.android.support'
        exclude group: 'com.android.volley'
        exclude group: 'org.jacoco'
    }
    implementation('com.mapbox.mapboxsdk:mapbox-android-sdk:6.5.0') {
        transitive = true
        exclude group: "com.android.support"
    }
    ...
}
```

## License

This software is provided under the Apache 2 license, see the LICENSE file for further details.

## Acknowledgements

We’d like to acknowledge The Bill and Melinda Gates Foundation and Qualcomm for supporting us in this work.
