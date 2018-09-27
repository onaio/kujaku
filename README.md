# Kujaku (Peacock) [![Build Status](https://travis-ci.org/onaio/kujaku.svg?branch=master)](https://travis-ci.org/onaio/kujaku)

A mapping and check-in library for Android using MapBox SDK.

## Setup Instructions

### Running Sample App

For instructions on how to run the sample app see [these instructions](./sample/README.md).

### Importing the Library

### How to publish artifacts

To publish new versions to the **Bintray/JFrog** account, run:

```
export BINTRAY_USER=johndoe
export BINTRAY_KEY=98sdfkmykeyhere90sdckl
./gradlew clean assembleRelease :library:bintrayUpload
./gradlew clean assembleRelease :utils:bintrayUpload

```

## License

This software is provided under the Apache 2 license, see the LICENSE file for further details.

## Acknowledgements

We’d like to acknowledge The Bill and Melinda Gates Foundation and Qualcomm for supporting us in this work.
