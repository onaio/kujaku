# Geospatial Widget (Kujaku) Specification

## Table of Contents

* [What is a Geospatial Widget?](#what-is-a-geospatial-widget)
* [Specification](#specification)
   * [Map Activity](#mapactivity)
   * [How to create a Mapbox style with Kujaku configuration](#how-to-create-a-mapbox-style-with-kujaku-configuration)
   * [Offline Maps Downloader Service](#offline-maps-downloader-service)
   * [Kujaku Map View](#kujaku-map-view)
   * [Helper Classes](#helper-classes)
     * [MapBoxStyleHelper class](#1-mapboxstylehelper)
     * [CoordinateUtils class](#2-coordinateutils)
     * [MapBoxWebServiceApi](#3-mapboxwebserviceapi)


## What is a Geospatial Widget?

The Geospatial widget is an Android SDK designed to connect to the georegistry and other common geographical data sources like OSM and OGC servers. It also supports visualisation of geosptial data and inspection. It is designed to integrate into common mobile data collection tools used in global health.  It is expected to support the following disease elimination use cases among others:
- Case Detection, Notification and Investigation
- Focus Investigation
- Routine and Reactive Intervention

The Geospatial widget library provides a map widget and has a map download service for offline support of map layers. It primarily uses the Mapbox SDK to implement its functionalities. The library also provides some helper util functions to support certain operations involving geospatial data.

# Specification

## MapActivity

The Geospatial widget SHOULD provide  a `MapActivity` activity that is used to display a map view given a Mapbox API access token and an array of Mapbox styles url. The `MapActivity` WILL be initialized through an intent request.
The constants below are required:

```java

    String PARCELABLE_KEY_MAPBOX_ACCESS_TOKEN;  // Mapbox API access token
    String[] PARCELABLE_KEY_MAPBOX_STYLES;  // Mapbox Styles (https://www.mapbox.com/mapbox-gl-js/style-spec/)
```

The following is what should be sent in the `PARCELABLE_KEY_MAPBOX_STYLES`:

Index 0 should have either of the following:
- A file path on the local storage eg. `file:///sdcard/MapboxStyles/nairobi-city-view.json`
- A Mapbox style URL eg. `mapbox://styles/ona/ksdk909kkscd9023k`
- A string of the JSON Object of an existing Mapbox Style or adhering to the Mapbox Style Spec

Index > 0 are ignored for now

The `MapActivity` should allow selection of a geospatial feature and post it back as a result. The geospatial feature SHOULD be in `GeoJSON` format.

Example usage:

1. Start an activity to show a Mapbox Style
```java
        Intent intent = new Intent(this, MapActivity.class);
        intent.putExtra(Constants.PARCELABLE_KEY_MAPBOX_STYLES, new String[]{
                "file:///sdcard/MapboxStyles/nairobi-city-view.json"
        });
        intent.putExtra(Constants.PARCELABLE_KEY_MAPBOX_ACCESS_TOKEN, "sdklcs823k9OIDFSKsd8uwk");

        startActivity(intent);
```


2. Start an activity with data

Go [here](#1-mapboxstylehelper) for more on the how to create a mapbox style with your own geospatial data

```java
        String mapboxStyleWithKujakuConfigAndData;
        ...

        Intent intent = new Intent(this, MapActivity.class);
        intent.putExtra(Constants.PARCELABLE_KEY_MAPBOX_STYLES, new String[]{
                mapboxStyleWithKujakuConfigAndData
        });
        intent.putExtra(Constants.PARCELABLE_KEY_MAPBOX_ACCESS_TOKEN, "sdklcs823k9OIDFSKsd8uwk");

        startActivity(intent);
```

3. Start an activity expecting callback in case a feature is selected

```java
        /*
        mapboxStyleWithKujakuConfigData is:
         - a String with the complete Mapbox Style or
         - a local path on the android device with the complete Mapbox Style
        */

        Intent intent = new Intent(this, MapActivity.class);
        intent.putExtra(Constants.PARCELABLE_KEY_MAPBOX_STYLES, new String[]{
                mapboxStyleWithKujakuConfigData
        });
        intent.putExtra(Constants.PARCELABLE_KEY_MAPBOX_ACCESS_TOKEN, "sdklcs823k9OIDFSKsd8uwk");

        startActivityForResult(intent, 43);
```

In case the user clicks on a feature, the info-window at the bottom is displayed to show more details on the feature. Clicking on the feature again is considered a `double-click` and this initiates the callback closing the activity. The activity returns a JSON Object accessible on `Intent` parameter of the `onActivityResult(int, int, Intent)` calling-activity method. The geoJSON feature is retrieved from the `String` extra with the key `geojson_feature`

For the `MapActivity` to respond to clicks on a feature, the feature requires to have:
- Specified in the **Kujaku config**
- Properties defined for it
- An `id` as one of the `properties`


## How to create a Mapbox style with Kujaku configuration

The aim is to stick to a certain spec i.e. add the Kujaku configuration as close to the Mapbox specification. [Here](sample_mapbox_style_with_kujaku_config.json) is a sample style with the Kujaku configuration.
The Kujaku Config enables the following capabilities:

1. Showing the information window - Activated on clicking a feature
2. Arranging the order of features in the information windows i.e. the order in which the features are listed
3. Specify which properties of a geoJSON feature to show in the information window and the labels to use for each feature
4. It also enables the callback in case a feature is double clicked by making the widget aware of the relevant data sources


Steps for creating the mapbox style with Kujaku configuration:

1. Add layers with preferred visual properties and name them appropriately(as per the Mapbox style spec)
2. Add your geospatial data to the Mapbox style in the form of geoJSON as per the Mapbox style spec
3. Add the Kujaku config

The Kujaku config is a JSON Object with the following:
1. `data_sources` JSON Array of `name`-only JSON Objects - The name points to the data source name in the style
```json
"data_sources": [
        {
          "name": "opensrp-custom-data-source-0"
        },
        {
          "name": "opensrp-custom-data-source-1"
        },
        {
          "name": "opensrp-custom-data-source-2"
        },
        {
          "name": "opensrp-custom-data-source-3"
        }
      ]
```

2. `sort_fields` JSON Array of JSON Objects(`type`, `data_field`). THe types can be `number`, `date` or `string`
```json
"sort_fields": [
        {
          "type": "date",
          "data_field": "client_reg_date"
        }
      ]
```

3. `info_window` JSON Object. This JSON Object contains a JSON Array with key `visible_properties`. The visibile properties array contains JSON Objects of `id` and `label` properties. The `id` is the key of the property in the feature while the label is what is shown on the info window as the property label
```json
"info_window": {
        "visible_properties": [
          {
            "id": "first_name",
            "label": "First Name"
          },
          {
            "id": "Birth_Weight",
            "label": "Birth Weight"
          },
          {
            "id": "Place_Birth",
            "label": "Place of Birth"
          },
          {
            "id": "zeir_id",
            "label": "ZEIR ID"
          }
        ]
      }
```


## Offline Maps Downloader Service

The Geospatial widget SHOULD provide the `MapboxOfflineDownloaderService` service that is used to download map layers for offline use. This service should also support the deletion of the offline map layers and resuming map layer download.
The service intent extras are as follows:

KEY | Type | Required | Description
--- | --- | --- | ---
`map_downloader_service` | `io.ona.kujaku.service.MapboxOfflineDownloaderService.SERVICE_ACTION` enum | Yes | Action to be performed. The service can either download(MapboxOfflineDownloaderService.SERVICE_ACTION.DOWNLOAD_MAP) or delete(MapboxOfflineDownloaderService.SERVICE_ACTION.DELETE_MAP) a downloaded map
`offline_map_unique_name` | String | Yes | Unique name for which the map will be referenced by
`mapbox_access_token` | String | Yes | This is required to access the Mapbox API
`offline_map_mapbox_style_url` | String | Yes | Required to access to download the map from the Mapbox API
`offline_map_max_zoom` | Double | Only for downloads | Specifies the max zoom level for the map assets to be downloaded
`offline_map_min_zoom` | Double | Only for downloads | Specifies the min zoom level for the map assets to be downloaded
`offline_map_top_left_bound` | Only for downloads | Yes | Specifies the top left bound of the map
`offline_map_bottom_right_bound` | Only for downloads | Yes | Specifies the bottom right bound of the map


The `MapboxOfflineDownloaderService` SHOULD post updates through a local broadcast with action `io.ona.kujaku.service.map.downloader.updates` `(Constants.INTENT_ACTION_MAP_DOWNLOAD_SERVICE_STATUS_UPDATES)`. The updates SHOULD have:


KEY | Mandatory | Constant in Library | Type | Description
--- | --- | --- | --- | ---
`RESULT STATUS` | Yes | `io.ona.kujaku.service.MapboxOfflineDownloaderService.KEY_RESULT_STATUS` | `io.ona.kujaku.service.MapboxOfflineDownloaderService.SERVICE_ACTION_RESULT` enum | which is either `io.ona.kujaku.service.MapboxOfflineDownloaderService.SERVICE_ACTION_RESULT.SUCCESSFUL` or `io.ona.kujaku.service.MapboxOfflineDownloaderService.SERVICE_ACTION_RESULT.FAILED`
`RESULT MESSAGE` | Yes | `io.ona.kujaku.service.MapboxOfflineDownloaderService.KEY_RESULT_MESSAGE` | String | a simple message, for example, the download percentage or task failure message.
`offline_map_unique_name`| Yes | `PARCELABLE_KEY_MAP_UNIQUE_NAME` | String | the map name
`RESULTS PARENT ACTION` | Yes | `KEY_RESULTS_PARENT_ACTION` |  `io.ona.kujaku.service.MapboxOfflineDownloaderService.SERVICE_ACTION` enum | Operation being performed on the map which is either a download or deletion

Sample code downloading a map for offline use:

```java

        double topLeftLat = 37.7897;
        double topLeftLng = -119.5073;
        double bottomRightLat = 37.6744;
        double bottomRightLng = -119.6815;

        Intent mapDownloadIntent = new Intent(this, MapboxOfflineDownloaderService.class);
        mapDownloadIntent.putExtra(Constants.PARCELABLE_KEY_MAPBOX_ACCESS_TOKEN, "sdklcs823k9OIDFSKsd8uwk");
        mapDownloadIntent.putExtra(Constants.PARCELABLE_KEY_SERVICE_ACTION, MapboxOfflineDownloaderService.SERVICE_ACTION.DOWNLOAD_MAP);
        mapDownloadIntent.putExtra(Constants.PARCELABLE_KEY_STYLE_URL, "mapbox://styles/ona/u89ukjhyvbnm");
        mapDownloadIntent.putExtra(Constants.PARCELABLE_KEY_MAP_UNIQUE_NAME, "kenya-malaria-spray-areas");
        mapDownloadIntent.putExtra(Constants.PARCELABLE_KEY_MAX_ZOOM, 20.0);
        mapDownloadIntent.putExtra(Constants.PARCELABLE_KEY_MIN_ZOOM, 0.0);
        mapDownloadIntent.putExtra(Constants.PARCELABLE_KEY_TOP_LEFT_BOUND, new LatLng(topLeftLat, topLeftLng));
        mapDownloadIntent.putExtra(Constants.PARCELABLE_KEY_BOTTOM_RIGHT_BOUND, new LatLng(bottomRightLat, bottomRightLng));
        Context.startService(mapDownloadIntent);
```

The **MapActivity** will request some permissions(during runtime & in the manifest) for it to work. The following are the permissions:

- `android.permission.ACCESS_FINE_LOCATION`- For the location to focus on the user's current location
- `android.permission.ACCESS_NETWORK_STATE`
- `android.permission.ACCESS_WIFI_STATE`
- `android.permission.READ_EXTERNAL_STORAGE` - Read cached mapbox styles on the device
- `android.permission.WRITE_EXTERNAL_STORAGE` - Cache mapbox styles on the device for offline use
- `android.permission.INTERNET` - Automatically permitted

## Kujaku Map View

The `KujakuMapView` enables a developer to have low level access to the geo-spatial widget. The developer can access the Mapbox APIs exposed on the mapbox `MapView` and have the flexibility to implement the widget in whatever view they want.

Example usage:
1. Add point without GPS

```java 
        kujakuMapView.addPoint(false, new AddPointCallback() {
            @Override
            public void onPointAdd(JSONObject jsonObject) {
                // Pick the new feature created as a result of chosen location
            }

            @Override
            public void onCancel() {
                // Do something here --> 
                // 1. Explain to the user that a location is required
                // 2. Give them the option of using GPS for their location
            }
        });
```

2. Add point with GPS

```java
        kujakuMapView.addPoint(true, new AddPointCallback() {
            @Override
            public void onPointAdd(JSONObject jsonObject) {
                // Make use of the new feature created as a result of chosen location
            }

            @Override
            public void onCancel() {
                // Do something here -->
                // 1. Explain to the user that a location is required
                // 2. Give them the option of manually locating the point
            }
        });


```

The JSONObject can be converted to `String` or used as is


## Helper Classes

The following helper classes will provide additional functionality to manipulate the data.

#### 1. MapBoxStyleHelper

This class enables you to:
- Add data sources to an existing Mapbox style
- Hide layers in the Mapbox style
- Add and generate kujaku configs to the Mapbox style
- Set the map center when the map loads
- Remove the map center if already added
- Generate a map center from bounds

The following is example code for how to create a Mapbox style with Kujaku configs

```java
        // mapboxStyle is a JSONObject of the Mapbox style
        String[] layersToHide = new String[]{"non-sprayed-areas", "swamps"};
        MapBoxStyleHelper mapBoxStyleHelper = new MapBoxStyleHelper(mapboxStyle);
        
        // This hides any layers not required
        mapBoxStyleHelper.disableLayers(layersToHide);
        
        // malariaSprayAreaDataSource
        String malariaSprayAreaLayer = "malaria-spray-area";
        String malariaSprayAreaDataSourceName = "malaria-spray-area-data-source";
        
        // missedSprayAreaDataSource is a JSONObject with `type` `geojson` and `data` property as a JSONObject FeatureCollection
        /* This is an example
        {
      "type": "geojson",
      "data": {
        "type": "FeatureCollection",
        "features": [
          {
            "type": "Feature",
            "geometry": {
              "type": "Point",
              "coordinates": [
                25.874258604205618,
                -17.86687127190279,
                0
              ]
            },
            "properties": {
              "id": "opensrp-custom-feature-0",
              "Birth_Weight": "2",
              "address2": "Gordons",
              "base_entity_id": "55b83f54-78f1-4991-8d12-813236ce39bb",
              "epi_card_number": "",
              "provider_id": "",
              "last_interacted_with": "1511875745328",
              "last_name": "Karis",
              "dod": "",
              "is_closed": "0",
              "gender": "Male",
              "lost_to_follow_up": "",
              "end": "2017-11-28 16:29:05",
              "Place_Birth": "Home",
              "inactive": "",
              "relational_id": "3d6b0d3a-e3ed-4146-8612-d8ac8ff84e8c",
              "client_reg_date": "2016-01-28T00:00:00.000Z",
              "geopoint": "0.3508685 37.5844647",
              "pmtct_status": "MSU",
              "address": "usual_residence",
              "start": "2017-11-28 16:27:06",
              "First_Health_Facility_Contact": "2017-11-28",
              "longitude": "37.5844647",
              "dob": "2017-09-28T00:00:00.000Z",
              "Home_Facility": "42abc582-6658-488b-922e-7be500c070f3",
              "date": "2017-11-28T00:00:00.000Z",
              "zeir_id": "1061647",
              "deviceid": "867104020633980",
              "addressType": "usual_residence",
              "latitude": "0.3508685",
              "provider_uc": "",
              "provider_location_id": "",
              "address3": "6c814e69-ed6f-4fcc-ac2c-8406508603f2",
              "first_name": "Frank 1"
            }
          },
          {
            "type": "Feature",
            "geometry": {
              "type": "Point",
              "coordinates": [
                25.855265422607058,
                -17.87051057660028,
                0
              ]
            },
            "properties": {
              "id": "opensrp-custom-feature-1",
              "Birth_Weight": "2",
              "address2": "Gordons",
              "base_entity_id": "55b83f54-78f1-4991-8d12-813236ce39bb",
              "epi_card_number": "",
              "provider_id": "",
              "last_interacted_with": "1511875745328",
              "last_name": "Karis",
              "dod": "",
              "is_closed": "0",
              "gender": "Male",
              "lost_to_follow_up": "",
              "end": "2017-11-28 16:29:05",
              "Place_Birth": "Home",
              "inactive": "",
              "relational_id": "3d6b0d3a-e3ed-4146-8612-d8ac8ff84e8c",
              "client_reg_date": "2016-02-28T00:00:00.000Z",
              "geopoint": "0.3508685 37.5844647",
              "pmtct_status": "MSU",
              "address": "usual_residence",
              "start": "2017-11-28 16:27:06",
              "First_Health_Facility_Contact": "2017-11-28",
              "longitude": "37.5844647",
              "dob": "2017-09-28T00:00:00.000Z",
              "Home_Facility": "42abc582-6658-488b-922e-7be500c070f3",
              "date": "2017-11-28T00:00:00.000Z",
              "zeir_id": "1061647",
              "deviceid": "867104020633980",
              "addressType": "usual_residence",
              "latitude": "0.3508685",
              "provider_uc": "",
              "provider_location_id": "",
              "address3": "6c814e69-ed6f-4fcc-ac2c-8406508603f2",
              "first_name": "Frank 2"
            }
          }
        ]
      }
    }
        
        */
        String missedSprayAreaLayer = "malaria-non-spray-area";
        String missedSprayAreaDataSourceName = "malaria-non-spray-area-data-source";
        
        JSONArray kujakuDataSourceNames = new JSONArray();
        
        // Add the malaria-spray-area data source
        mapBoxStyleHelper.insertGeoJsonDataSource(malariaSprayAreaDataSource, missedSprayAreaDataSource, malariaSprayAreaDataSourceName);
        kujakuDataSourceNames.put(malariaSprayAreaDataSourceName);
        
        mapBoxStyleHelper.insertGeoJsonDataSource(missedSprayAreaLayer, missedSprayAreaDataSource, missedSprayAreaDataSourceName);
        kujakuDataSourceNames.put(missedSprayAreaDataSourceName);

        // kujakuConfig is a JSONObject with the key(s) `data_sources`, `sort_fields` and/or `info_window` each holding the appropriate data
        if (kujakuConfig != null) {
            // Add correct source layer names
            kujakuConfig.put("data_source_names", kujakuDataSourceNames);
            mapBoxStyleHelper.insertKujakuConfig(kujakuConfig);
        }

        String finalMapboxStyleWithKujakuConfigs = mapboxStyleHelper.build().toString();
```


#### 2. CoordinateUtils

This class provides you with methods for:
1. Checking if a location is in certain bounds

#### 3. MapBoxWebServiceApi

This class enables you to interact with the Mapbox API so that you can retrieve a style JSON using only the styleId. You can then use use the style string obtained to create a Mapbox style with geosptial data

