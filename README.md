# Kujaku (Peacock) ![Build Status](https://github.com/onaio/kujaku/actions/workflows/ci.yml/badge.svg) ![Download](https://badgen.net/maven/v/maven-central/io.ona.kujaku/library) [![Coverage Status](https://coveralls.io/repos/github/onaio/kujaku/badge.svg)](https://coveralls.io/github/onaio/kujaku)

A mapping and check-in library for Android using **MapBox SDK (Version 8.3.3)** 

## IMPORTANT UPDATE

**Kujaku library and utils artefacts are no longer available on bintray and any builds using these dependencies will fail. Kindly update to use `mavenCentral()` repository in your `build.gradle` and the `library` version `0.9.0` for a successful build. Kindly [create an issue](https://github.com/onaio/kujaku/issues/new) in case you face any problems.**


# Table of Contents

* [Setup Instructions](#setup-instructions)
  * [Running the Sample App](#running-sample-app)
  * [Importing the Library](#importing-the-library)
  * [How to publish artifacts](#how-to-publish-artifacts)
* [How to import the library](#how-to-import-the-library)
* [How to use the library](#how-to-use-the-library)
* [License](#license)
* [Acknowledgements](#acknowledgements)
* [Specification](SPECIFICATION.md)

## Setup Instructions

### Running Sample App

For instructions on how to run the sample app see [these instructions](./sample/README.md).

### Importing the Library

### How to publish artifacts

You can easily publish a snapshot artefact by creating a tag for the library or util module. Follow the steps below:

#### To publish the utils module
1. Update the `utils` version in this file `utils\build.gradle` in the format `X.Y.Z-SNAPSHOT` where X, Y and Z should be replaced with the version numbers of the major vesion, minor version and build versions respectively. 
2. Create a PR with the change and have it merged
3. Generate a tag with the title `utils-vX.Y.X-SNAPSHOT` and push it. This will trigger a publish of the artefact snapshot version to Sonatype and Github packages

#### To publish the library module

1. Follow the steps above to get the latest version of `utils` as a dependency in your library snapshot
2. Update the `library` version in this file `library\build.gradle` in the format `X.Y.Z-SNAPSHOT` where X, Y and Z should be replaced with the version numbers of the major vesion, minor version and build versions respectively. 
2. Create a PR with the change and have it merged
3. Generate a tag with the title `library-vX.Y.X-SNAPSHOT` and push it. This will trigger a publish of the artefact snapshot version to Sonatype and Github packages

- Due to the sunsetting of JFrog Bintray, Kujaku artefacts/release are no longer available on bintray and all publishing is done to Maven Central. Some of the packages are also published to Github packages.

## How to import the library

To import the library:

1. Add the following snippet to your app module `build.gradle`

```
...


allprojects {
    repositories {
        mavenCentral()
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
    implementation 'io.ona.kujaku:library:0.9.0'
    ...
}
```

## How to use the library

The library offers a view `KujakuMapView` that provides more functionality than Mapbox. 
- For Mapbox related functionality, [go here](https://docs.mapbox.com/android/maps/overview/)
- For setup information and extra features provided by this library [go here](./SPECIFICATION.md)

## `Unable to resolve artifact: Missing` while running tests

This is encountered when Robolectric has problems downloading the jars it needs for different Android SDK levels. If you keep running into this you can download the JARs locally and point Robolectric to them by doing:

```
./download-robolectric-deps.sh

## License

This software is provided under the Apache 2 license, see the [LICENSE file](LICENSE) for further details.

## Acknowledgements

We’d like to acknowledge The Bill and Melinda Gates Foundation and Qualcomm for supporting us in this work.
