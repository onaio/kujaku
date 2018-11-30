package io.ona.kujaku.sample.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.offline.OfflineManager;
import com.mapbox.mapboxsdk.offline.OfflineRegion;
import com.mapbox.mapboxsdk.offline.OfflineRegionError;
import com.mapbox.mapboxsdk.offline.OfflineRegionStatus;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import es.dmoral.toasty.Toasty;
import io.ona.kujaku.activities.MapActivity;
import io.ona.kujaku.sample.BuildConfig;
import io.ona.kujaku.sample.R;
import io.ona.kujaku.utils.Constants;
import io.ona.kujaku.utils.LogUtil;
import io.ona.kujaku.utils.helpers.MapBoxStyleHelper;

import static io.ona.kujaku.utils.Constants.MAP_ACTIVITY_REQUEST_CODE;
import static io.ona.kujaku.utils.IOUtil.readInputStreamAsString;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 05/02/2018.
 */

public class OfflineRegionsActivity extends BaseNavigationDrawerActivity {

    private static final String TAG = OfflineRegionsActivity.class.getName();
    private boolean activated[];
    private String currentText = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.offline_regions_activity_title);

        ListView listView = findViewById(R.id.lv_offlineRegionsActivity_mapsList);
        getOfflineDownloadedRegions(listView);
    }

    private void getOfflineDownloadedRegions(final ListView listView) {
        Mapbox.getInstance(this, BuildConfig.MAPBOX_SDK_ACCESS_TOKEN);

        OfflineManager offlineManager = OfflineManager.getInstance(OfflineRegionsActivity.this);
        offlineManager.listOfflineRegions(new OfflineManager.ListOfflineRegionsCallback() {
            @Override
            public void onList(final OfflineRegion[] offlineRegions) {
                if (offlineRegions == null || offlineRegions.length == 0) {
                    Toasty.info(OfflineRegionsActivity.this, getString(R.string.you_do_not_have_offline_regions), Toast.LENGTH_LONG)
                            .show();
                } else {
                    String[] offlineRegionsInfo = getOfflineRegionsInfo(offlineRegions);
                    renderOfflineRegions(listView, offlineRegionsInfo, offlineRegions);
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "ERROR :: "  + error);
            }
        });
    }

    private String[] getOfflineRegionsInfo(final OfflineRegion[] offlineRegions) {
        String[] offlineRegionsInfoArray = new String[offlineRegions.length];

        for(int position = 0; position < offlineRegions.length; position++) {
            //Add name
            //Add percentage downloaded
            //Add date & other details
            //Add unique id
            StringBuilder offlineRegionInfo = new StringBuilder();
            byte[] metadataBytes = offlineRegions[position].getMetadata();
            try {
                JSONObject jsonObject = new JSONObject(new String(metadataBytes));
                if (jsonObject.has("FIELD_REGION_NAME")) {
                    offlineRegionInfo.append("REGION NAME: ");
                    offlineRegionInfo.append(jsonObject.getString("FIELD_REGION_NAME"));
                }

                if (jsonObject.has("JSON_FIELD_REGION_NAME")) {
                    offlineRegionInfo.append("\nREGION NAME: ");
                    offlineRegionInfo.append(jsonObject.getString("JSON_FIELD_REGION_NAME"));
                }

            } catch (JSONException e) {
                LogUtil.e(TAG, e);
            }

            offlineRegionInfo.append("\nID: ");
            offlineRegionInfo.append(offlineRegions[position].getID());
            offlineRegionsInfoArray[position] = offlineRegionInfo.toString();
        }

        return offlineRegionsInfoArray;
    }

    private void renderOfflineRegions(@NonNull ListView listView, String[] offlineRegionsInfo, OfflineRegion[] offlineRegions) {
        activated = new boolean[offlineRegionsInfo.length];
        listView.setAdapter(new ArrayAdapter(OfflineRegionsActivity.this, R.layout.offline_region_item, offlineRegionsInfo){
            @NonNull
            @Override
            public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(R.layout.offline_region_item, parent, false);
                }

                final TextView textView = convertView.findViewById(R.id.tv_offlineRegionItem_text);
                final Button navigateToMapBtn = convertView.findViewById(R.id.btn_offlineRegionItem_navigateToMap);

                textView.setText(offlineRegionsInfo[position]);
                textView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!activated[position]) {
                            offlineRegions[position].setDownloadState(OfflineRegion.STATE_ACTIVE);
                        } else {
                            offlineRegions[position].setDownloadState(OfflineRegion.STATE_INACTIVE);
                        }

                        activated[position] = !activated[position];
                        offlineRegions[position].setObserver(new OfflineRegion.OfflineRegionObserver() {
                            @Override
                            public void onStatusChanged(OfflineRegionStatus status) {
                                double progress = (100.0 * status.getCompletedResourceCount()) / status.getRequiredResourceCount();

                                textView.setText(offlineRegionsInfo[position] + "\nPROGRESS : " + progress + "%");

                                if (status.isComplete()) {
                                    navigateToMapBtn.setVisibility(View.VISIBLE);
                                }
                            }

                            @Override
                            public void onError(OfflineRegionError error) {
                                textView.setText(offlineRegionsInfo[position] + "\nError : " + error.getReason() + "\n" + error.getMessage());
                            }

                            @Override
                            public void mapboxTileCountLimitExceeded(long limit) {
                                textView.setText(offlineRegionsInfo[position] + "\nMapbox tile limit count exceeded");
                            }
                        });

                    }
                });

                navigateToMapBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openOfflineRegionMap(offlineRegions[position].getDefinition().getBounds().getCenter());
                    }
                });

                offlineRegions[position].getStatus(new OfflineRegion.OfflineRegionStatusCallback() {
                    @Override
                    public void onStatus(OfflineRegionStatus status) {
                        offlineRegionsInfo[position] += "\nDOWNLOADED SIZE : " + (status.getCompletedResourceSize() / 1e6) + " MB";
                        double percentageDownload = (100.0 * status.getCompletedResourceCount()) / status.getRequiredResourceCount();

                        offlineRegionsInfo[position] += "\nDOWNLOADED %: " + percentageDownload + "%";
                        offlineRegionsInfo[position] += "\nCOMPLETED RESOURCES : " + status.getCompletedResourceCount();
                        offlineRegionsInfo[position] += "\nREQUIRED RESOURCES : " + status.getRequiredResourceCount();
                        offlineRegionsInfo[position] += "\n";

                        Log.i(TAG, offlineRegionsInfo[position]);

                        if (status.isComplete()) {
                            navigateToMapBtn.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onError(String error) {
                        offlineRegionsInfo[position] += "\nDOWNLOADED STATUS: GET ERROR - " + error;
                        Log.e(TAG, "OFFLINE REGION STATUS : " + error);
                    }
                });

                return convertView;
            }
        });
    }

    private void openOfflineRegionMap(@NonNull LatLng mapCenter) {
        try {
            String mapboxStyle = readInputStreamAsString(getAssets().open("new-reveal-style-with-satellite.json"));
            JSONObject mapboxStyleJSON = new JSONObject(mapboxStyle);
            mapboxStyleJSON.put(MapBoxStyleHelper.KEY_ROOT_ZOOM, 16.76);

            JSONArray mapCenterCoordinates = new JSONArray();
            mapCenterCoordinates.put(mapCenter.getLongitude());
            mapCenterCoordinates.put(mapCenter.getLatitude());

            mapboxStyleJSON.put(MapBoxStyleHelper.KEY_MAP_CENTER, mapCenterCoordinates);

            Intent intent = new Intent(this, MapActivity.class);
            intent.putExtra(Constants.PARCELABLE_KEY_MAPBOX_STYLES, new String[]{mapboxStyleJSON.toString()});
            intent.putExtra(Constants.PARCELABLE_KEY_MAPBOX_ACCESS_TOKEN, BuildConfig.MAPBOX_SDK_ACCESS_TOKEN);

            startActivityForResult(intent, MAP_ACTIVITY_REQUEST_CODE);
        } catch (IOException | JSONException e) {
            LogUtil.e(TAG, e);
            Toasty.error(this, getString(R.string.error_occurred_reading_mapbox_style))
                    .show();
        }
    }

    @Override
    protected int getContentView() {
        return R.layout.offline_regions_activity;
    }

    @Override
    protected int getSelectedNavigationItem() {
        return R.id.nav_offline_regions;
    }
}
