package io.ona.kujaku.downloaders;

import android.content.Context;
import android.util.Log;

import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.offline.OfflineManager;
import com.mapbox.mapboxsdk.offline.OfflineRegion;
import com.mapbox.mapboxsdk.offline.OfflineRegionError;
import com.mapbox.mapboxsdk.offline.OfflineRegionStatus;
import com.mapbox.mapboxsdk.offline.OfflineTilePyramidRegionDefinition;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import io.ona.kujaku.listeners.IncompleteMapDownloadCallback;
import io.ona.kujaku.listeners.OfflineRegionObserver;
import io.ona.kujaku.listeners.OnDownloadMapListener;
import io.ona.kujaku.listeners.OnPauseMapDownloadCallback;
import utils.exceptions.OfflineMapDownloadException;

/**
 *
 * This is a singleton
 *
 * Basically wraps around the MapBox OfflineManager & enables:
 *  - Downloading a map for offline
 *  - Pausing a download
 *  - Resuming download
 *  - Deleting an offline map
 *  - Getting the map status {@link com.mapbox.mapboxsdk.offline.OfflineRegionStatus}
 *
 *
 * Created by Ephraim Kigamba - ekigamba@ona.io on 10/11/2017.
 */
public class MapBoxOfflineResourcesDownloader {

    //Should this be a singleton ???
    private static MapBoxOfflineResourcesDownloader instance = null;
    private Context context;
    private Mapbox mapbox;
    private OfflineManager offlineManager;
    private static final String TAG = MapBoxOfflineResourcesDownloader.class.getSimpleName();

    // JSON encoding/decoding
    public static final String JSON_CHARSET = "UTF-8";
    public static final String JSON_FIELD_REGION_NAME = "FIELD_REGION_NAME";


    public static MapBoxOfflineResourcesDownloader getInstance(Context context, String accessToken) {
        return getInstance(context, Mapbox.getInstance(context, accessToken));
    }

    public static MapBoxOfflineResourcesDownloader getInstance(Context context, Mapbox mapbox) {
        if (instance == null) {
            instance = new MapBoxOfflineResourcesDownloader(context, mapbox);
        }

        return instance;
    }

    private MapBoxOfflineResourcesDownloader(Context context, Mapbox mapbox) {
        this.context = context;
        this.mapbox = mapbox;

        if (context != null) {
            offlineManager = OfflineManager.getInstance(context);
        }
    }

    /**
     * Basically downloads/queues the map for download
     *
     *
     *
     * @param name Unique name of the map
     * @param styleUrl The Style URL on MapBox
     * @param topLeftBound The top-left coordinate of the map
     * @param bottomRightBound The bottom-right coordinate of the map
     * @param minZoom The min-zoom of the map i.e among 0-22
     * @param maxZoom The max-zoom of the map i.e. among 0-22. This should be greater than the {@code minZoom}
     * @param onDownloadMapListener {@link OnDownloadMapListener} to provide updates/errors during MapDownload
     *
     * @throws OfflineMapDownloadException In case - {@code name} is {@code NULL} or empty, already used by another offline map (not unique)
     *                                             - {@code styleUrl} is invalid - {@code NULL}, empty OR not a MapBox url i.e. in the form mapbox://
     *                                             - {@code minZoom} is invalid - Greater than maxZoom, not among 0-22
     *                                             - {@code maxZoom} is invalid - Lower than minZoom, not among 0-22
     */
    public void downloadMap(final String name, final String styleUrl, final LatLng topLeftBound, final LatLng bottomRightBound, final double minZoom, final double maxZoom, final OnDownloadMapListener onDownloadMapListener)
            throws OfflineMapDownloadException {
        if (offlineManager == null) {
            throw new OfflineMapDownloadException("Context passed is null");
        }

        if (name == null || name.isEmpty()) {
            throw new OfflineMapDownloadException("Invalid map name");
        }

        if (styleUrl == null || styleUrl.isEmpty() || !styleUrl.contains("mapbox://")) {
            throw new OfflineMapDownloadException("Invalid Style URL");
        }

        if (minZoom < 0 || minZoom > 22 || maxZoom < 0 || maxZoom > 22) {
            throw new OfflineMapDownloadException("maxZoom & minZoom should be among 0-22");
        }

        if (minZoom > maxZoom) {
            throw new OfflineMapDownloadException("minZoom should be lower than maxZoom");
        }

        offlineManager.listOfflineRegions(new OfflineManager.ListOfflineRegionsCallback() {
            @Override
            public void onList(OfflineRegion[] offlineRegions) {
                OfflineRegion similarOfflineRegion = getOfflineRegion(name, offlineRegions);
                if (similarOfflineRegion != null && onDownloadMapListener != null) {
                    onDownloadMapListener.onError("Map Already Exists", "The map name provided already exists");
                    return;
                }

                // Download the map
                byte[] metadata = new byte[0];
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put(JSON_FIELD_REGION_NAME, name);
                    metadata = jsonObject.toString().getBytes(JSON_CHARSET);

                    LatLngBounds latLngBounds = new LatLngBounds.Builder()
                            .include(topLeftBound)
                            .include(bottomRightBound)
                            .build();

                    OfflineTilePyramidRegionDefinition offlineMapDefinition = new OfflineTilePyramidRegionDefinition(
                            styleUrl,
                            latLngBounds,
                            minZoom,
                            maxZoom,
                            context.getResources().getDisplayMetrics().density);

                    offlineManager.createOfflineRegion(offlineMapDefinition, metadata, new OfflineManager.CreateOfflineRegionCallback() {
                        @Override
                        public void onCreate(OfflineRegion offlineRegion) {
                            resumeMapDownload(offlineRegion, onDownloadMapListener);
                        }

                        @Override
                        public void onError(String error) {
                            if(onDownloadMapListener != null) {
                                onDownloadMapListener.onError(error, "");
                            }
                        }
                    });
                } catch (UnsupportedEncodingException e) {
                    Log.e(TAG, Log.getStackTraceString(e));
                    if(onDownloadMapListener != null) {
                        onDownloadMapListener.onError(e.getMessage(), Log.getStackTraceString(e));
                    }
                } catch (JSONException e) {
                    Log.e(TAG, Log.getStackTraceString(e));
                    if(onDownloadMapListener != null) {
                        onDownloadMapListener.onError(e.getMessage(), Log.getStackTraceString(e));
                    }
                }
            }

            @Override
            public void onError(String error) {
                if(onDownloadMapListener != null) {
                    onDownloadMapListener.onError(error, "");
                }
            }
        });

    }

    /**
     * Deletes a specific offline map given the name
     *
     *
     * @param name Unique name of the map
     * @param offlineRegionDeleteCallback Callback in case the operation is SUCCESS or FAILURE {@see com.mapbox.mapboxsdk.offline.OfflineRegion.OfflineRegionDeleteCallback}
     *                                    Fails if the offline map with the give {@code name} does not exist
     */
    public void deleteMap(final String name, final OfflineRegion.OfflineRegionDeleteCallback offlineRegionDeleteCallback) {
        if (offlineManager == null) {
            if(offlineRegionDeleteCallback != null) {
                offlineRegionDeleteCallback.onError("Context passed is null");
            }
        }

        offlineManager.listOfflineRegions(new OfflineManager.ListOfflineRegionsCallback() {
            @Override
            public void onList(OfflineRegion[] offlineRegions) {
                OfflineRegion offlineRegion = getOfflineRegion(name, offlineRegions);
                if (offlineRegion == null) {
                    if(offlineRegionDeleteCallback != null) {
                        offlineRegionDeleteCallback.onError("Map could not be found");
                    }
                    return;
                }

                OfflineRegion.OfflineRegionDeleteCallback nonNullOfflineRegionCallback;
                if (offlineRegionDeleteCallback == null) {
                    nonNullOfflineRegionCallback = new OfflineRegion.OfflineRegionDeleteCallback() {
                        @Override
                        public void onDelete() {
                            Log.i(TAG, "ON DELETE MAP {" + name + "} SUCCESS");
                        }

                        @Override
                        public void onError(String error) {
                            Log.e(TAG, "ON DELETE MAP : " + name + " --> "+ error);
                        }
                    };
                } else {
                    nonNullOfflineRegionCallback = offlineRegionDeleteCallback;
                }
                offlineRegion.delete(nonNullOfflineRegionCallback);
            }

            @Override
            public void onError(String error) {
                if(offlineRegionDeleteCallback != null) {
                    offlineRegionDeleteCallback.onError(error);
                }
            }
        });
    }

    /**
     * Resumes download of an Offline map with the given name
     *
     * @param name Unique name of the map
     * @param onDownloadMapListener Callback to give updates on download progress or errors
     */
    public void resumeMapDownload(final String name, final OnDownloadMapListener onDownloadMapListener) {
        if (offlineManager == null) {
            onDownloadMapListener.onError("Context passed is null", "");
        }

        offlineManager.listOfflineRegions(new OfflineManager.ListOfflineRegionsCallback() {
            @Override
            public void onList(OfflineRegion[] offlineRegions) {
                final OfflineRegion offlineRegion = getOfflineRegion(name, offlineRegions);
                if (offlineRegion == null) {
                    if (onDownloadMapListener != null) {
                        onDownloadMapListener.onError("Map could not be found", "");
                    }
                    return;
                }

                offlineRegion.getStatus(new OfflineRegion.OfflineRegionStatusCallback() {
                    @Override
                    public void onStatus(OfflineRegionStatus status) {
                        if (status.isComplete()) {
                            if (onDownloadMapListener != null) {
                                onDownloadMapListener.onError("Map Download is already complete", "");
                            }
                        } else if (status.getDownloadState() == OfflineRegion.STATE_ACTIVE) {
                            if (onDownloadMapListener != null) {
                                onDownloadMapListener.onError("Map is already downloading", "");
                            }
                        } else {
                            // Resume map download here
                            resumeMapDownload(offlineRegion, onDownloadMapListener);
                        }
                    }

                    @Override
                    public void onError(String error) {
                        if (onDownloadMapListener != null) {
                            onDownloadMapListener.onError(error, "");
                        }
                    }
                });
            }

            @Override
            public void onError(String error) {
                if (onDownloadMapListener != null) {
                    onDownloadMapListener.onError(error, "");
                }
            }
        });
    }

    /**
     * Resumes download of an Offline map given the name
     *
     * @param offlineRegion {@link OfflineRegion} to resume download
     * @param onDownloadMapListener {@link OnDownloadMapListener} Callback to receive map download updates or error description
     */
    public void resumeMapDownload(OfflineRegion offlineRegion, final OnDownloadMapListener onDownloadMapListener) {
        offlineRegion.setDownloadState(OfflineRegion.STATE_ACTIVE);
        offlineRegion.setObserver(new OfflineRegion.OfflineRegionObserver() {
            @Override
            public void onStatusChanged(OfflineRegionStatus status) {
                if (onDownloadMapListener != null) {
                    onDownloadMapListener.onStatusChanged(status);
                }
            }

            @Override
            public void onError(OfflineRegionError error) {
                if (onDownloadMapListener != null) {
                    onDownloadMapListener.onError(error.getMessage(), error.getReason());
                }
            }

            @Override
            public void mapboxTileCountLimitExceeded(long limit) {
                if (onDownloadMapListener != null) {
                    onDownloadMapListener.onError("MapBox Tile count " + limit + " limit exceeded", "Checkout https://www.mapbox.com/help/mobile-offline/ for more");
                }
            }
        });
    }

    /**
     * Retrieves all incomplete map downloads
     *
     * @param incompleteMapDownloadCallback Callback called when incomplete map downloads are found
     */
    public void getIncompleteMapDownloads(final IncompleteMapDownloadCallback incompleteMapDownloadCallback) {
        if (offlineManager == null) {
            incompleteMapDownloadCallback.onError("Context passed is null", "");
            return;
        }

        offlineManager.listOfflineRegions(new OfflineManager.ListOfflineRegionsCallback() {
            @Override
            public void onList(OfflineRegion[] offlineRegions) {
                for(final OfflineRegion offlineRegion: offlineRegions) {
                    offlineRegion.getStatus(new OfflineRegion.OfflineRegionStatusCallback() {
                        @Override
                        public void onStatus(OfflineRegionStatus status) {
                            if (!status.isComplete()) {
                                incompleteMapDownloadCallback.incompleteMap(offlineRegion, status);
                            }
                        }

                        @Override
                        public void onError(String error) {
                            incompleteMapDownloadCallback.onError(error, "");
                        }
                    });
                }
            }

            @Override
            public void onError(String error) {
                incompleteMapDownloadCallback.onError(error, "");
            }
        });
    }

    /**
     * Pauses a map download
     * @param name Unique name of the map
     * @param onPauseMapDownloadCallback Callback which is called in case the operation is a SUCCESS or FAILURE
     */
    public void pauseMapDownload(final String name, final OnPauseMapDownloadCallback onPauseMapDownloadCallback) {
        if (offlineManager == null) {
            onPauseMapDownloadCallback.onPauseError(OnPauseMapDownloadCallback.CONTEXT_PASSED_IS_NULL, "");
            return;
        }

        offlineManager.listOfflineRegions(new OfflineManager.ListOfflineRegionsCallback() {
            @Override
            public void onList(OfflineRegion[] offlineRegions) {
                final OfflineRegion offlineRegion = getOfflineRegion(name, offlineRegions);
                if (offlineRegion == null) {
                    onPauseMapDownloadCallback.onPauseError(OnPauseMapDownloadCallback.MAP_COULD_NOT_BE_FOUND, "");
                    return;
                }

                offlineRegion.getStatus(new OfflineRegion.OfflineRegionStatusCallback() {
                    @Override
                    public void onStatus(OfflineRegionStatus status) {
                        if (status.getDownloadState() == OfflineRegion.STATE_ACTIVE) {
                            offlineRegion.setDownloadState(OfflineRegion.STATE_INACTIVE);
                            onPauseMapDownloadCallback.onPauseSuccess();
                        } else {
                            if (status.isComplete()) {
                                onPauseMapDownloadCallback.onPauseError(OnPauseMapDownloadCallback.MAP_DOWNLOAD_COMPLETE, "");
                            } else {
                                onPauseMapDownloadCallback.onPauseError(OnPauseMapDownloadCallback.MAP_WAS_NOT_DOWNLOADING, "");
                            }
                        }
                    }

                    @Override
                    public void onError(String error) {
                        onPauseMapDownloadCallback.onPauseError(error, "");
                    }
                });
            }

            @Override
            public void onError(String error) {
                onPauseMapDownloadCallback.onPauseError(error, "");
            }
        });
    }

    /**
     * Retrieves an Offline Map's status {@see OfflineRegionStatus}
     *
     * @param name Unique name of the map
     * @param offlineRegionObserver Callback called when map status is retrieved or the operation FAILS
     */
    public void getMapStatus(final String name, final OfflineRegionObserver offlineRegionObserver) {
        if (offlineManager == null) {
            offlineRegionObserver.onError("Context passed is null", "");
        } else {
            offlineManager.listOfflineRegions(new OfflineManager.ListOfflineRegionsCallback() {
                @Override
                public void onList(OfflineRegion[] offlineRegions) {
                    final OfflineRegion offlineRegion = getOfflineRegion(name, offlineRegions);
                    if (offlineRegion == null) {
                        offlineRegionObserver.onError("Map could not be found : " + name, "");
                    } else {
                        offlineRegion.getStatus(new OfflineRegion.OfflineRegionStatusCallback() {
                            @Override
                            public void onStatus(OfflineRegionStatus status) {
                                offlineRegionObserver.onStatusChanged(status);
                            }

                            @Override
                            public void onError(String error) {
                                offlineRegionObserver.onError(error, "");
                            }
                        });
                    }
                }

                @Override
                public void onError(String error) {
                    offlineRegionObserver.onError(error, "");
                }
            });
        }
    }

    /**
     * Matches a given map name to an OfflineRegion given the OfflineRegions
     * @param name Unique name of the map
     * @param offlineRegions OfflineRegions returned from {@link OfflineManager#listOfflineRegions(OfflineManager.ListOfflineRegionsCallback)}
     * @return {@link OfflineRegion} with the given name
     */
    private OfflineRegion getOfflineRegion(String name, OfflineRegion[] offlineRegions) {
        for(OfflineRegion offlineRegion: offlineRegions) {
            try {
                String json = new String(offlineRegion.getMetadata(), JSON_CHARSET);
                JSONObject jsonObject = new JSONObject(json);
                if (jsonObject.has(JSON_FIELD_REGION_NAME)) {
                    String regionName = jsonObject.getString(JSON_FIELD_REGION_NAME);
                    if (name.equals(regionName)) {
                        return offlineRegion;
                    }
                }

            } catch (UnsupportedEncodingException e) {
                Log.e(TAG, Log.getStackTraceString(e));
                // Just move to the next map
            } catch (JSONException e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }

        }

        return null;
    }

}
