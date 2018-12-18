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
./gradlew clean assembleRelease :utils:bintrayUpload
./gradlew clean assembleRelease :library:bintrayUpload

```

To publish locally:

```
./gradlew clean assembleRelease :utils:publishToMavenLocal
./gradlew clean assembleRelease :library:publishToMavenLocal

```

## WMTS Layers

### WmtsService

This class reads the capabilities Xml stream and deserialize it into a WMTSCapabilities object.
You need to provide a Capabilities URL as argument to the constructor

```
 WmtsCapabilitiesService wmtsService = new WmtsCapabilitiesService(getString(R.string.wmts_capabilities_url));
```

Calls the requestData method to retrieve the Capabilities information and set a listener that will be called as soon as the asynch task return the result.

```
wmtsService.requestData();

wmtsService.setListener(new WmtsCapabilitiesListener() {
    @Override
    public void onCapabilitiesReceived(WmtsCapabilities capabilities) {
        try {
            // kujakuMapView.addWmtsLayer(capabilities);
        }
        catch (Exception ex) {
            //throw ex;
        }
    }
});
```

### Add WMTS layers to the map

The kujakuMapView has 4 public methods to add Layers onto the map :

* This method will add the first layer of the Capabilities file with the default style and first tileMatrixSet :
```
public void addWmtsLayer(WmtsCapabilities capabilities) throws Exception
```

* This method will add the layer identified by the layerIdentifier argument of the Capabilities file with the default style and first tileMatrixSet :
```
public void addWmtsLayer(WmtsCapabilities capabilities, String layerIdentifier) throws Exception
```

* This method will add the layer identified by the layerIdentifier argument of the Capabilities file with the identified style by the styleIdentifier argument and first tileMatrixSet:
```
 public void addWmtsLayer(WmtsCapabilities capabilities, String layerIdentifier, String styleIdentifier) throws Exception
```

* This method will add the layer identified by the layerIdentifier argument of the Capabilities file with the identified style by the styleIdentifier argument and the identified tileMatrixSet by the tileMatrixSetLinkIdentifier argument:
```
public void addWmtsLayer(WmtsCapabilities capabilities, String layerIdentifier, String styleIdentifier, String tileMatrixSetLinkIdentifier) throws Exception
```


## License

This software is provided under the Apache 2 license, see the LICENSE file for further details.

## Acknowledgements

We’d like to acknowledge The Bill and Melinda Gates Foundation and Qualcomm for supporting us in this work.
