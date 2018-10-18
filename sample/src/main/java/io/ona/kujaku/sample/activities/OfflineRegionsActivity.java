package io.ona.kujaku.sample.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.offline.OfflineManager;
import com.mapbox.mapboxsdk.offline.OfflineRegion;
import com.mapbox.mapboxsdk.offline.OfflineRegionError;
import com.mapbox.mapboxsdk.offline.OfflineRegionStatus;

import org.json.JSONException;
import org.json.JSONObject;

import es.dmoral.toasty.Toasty;
import io.ona.kujaku.sample.BuildConfig;
import io.ona.kujaku.sample.R;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 05/02/2018.
 */

public class OfflineRegionsActivity extends BaseNavigationDrawerActivity {

    private static final String TAG = OfflineRegionsActivity.class.getName();
    private String offlineRegionInfo = "";
    private int position = 0;
    private boolean activated = false;
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

                if (offlineRegions == null || offlineRegions.length < 1) {
                    Toasty.info(OfflineRegionsActivity.this, getString(R.string.you_do_not_have_offline_regions), Toast.LENGTH_LONG)
                            .show();
                }

                getOfflineRegionsInfo(offlineRegions, listView);
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "ERROR :: "  + error);
            }
        });
    }

    private void getOfflineRegionsInfo(final OfflineRegion[] offlineRegions, final ListView listView) {
        final String[] offlineInfo = new String[offlineRegions.length + 1];

        for(position = 0; position < offlineRegions.length; position++) {

            //Add name
            //Add percentage downloaded
            //Add date & other details
            //Add unique id
            offlineRegionInfo = "";
            byte[] metadataBytes = offlineRegions[position].getMetadata();
            try {
                JSONObject jsonObject = new JSONObject(new String(metadataBytes));
                if (jsonObject.has("FIELD_REGION_NAME")) {
                    offlineRegionInfo += "REGION NAME: " + jsonObject.getString("FIELD_REGION_NAME");
                }

                if (jsonObject.has("JSON_FIELD_REGION_NAME")) {
                    offlineRegionInfo += "\nREGION NAME: " + jsonObject.getString("JSON_FIELD_REGION_NAME");
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            offlineRegions[position].getStatus(new OfflineRegion.OfflineRegionStatusCallback() {
                @Override
                public void onStatus(OfflineRegionStatus status) {
                    offlineInfo[OfflineRegionsActivity.this.position] += "\nDOWNLOADED SIZE : " + (status.getCompletedResourceSize() / 1e6) + " MB";
                    double percentageDownload = (100.0 * status.getCompletedResourceCount()) / status.getRequiredResourceCount();

                    offlineInfo[OfflineRegionsActivity.this.position] += "\nDOWNLOADED %: " + percentageDownload + "%";
                    offlineInfo[position] += "\nCOMPLETED RESOURCES : " + status.getCompletedResourceCount();
                    offlineInfo[position] += "\nREQUIRED RESOURCES : " + status.getRequiredResourceCount();
                    offlineInfo[position] += "\n";

                    Log.i(TAG, offlineRegionInfo);

                    listView.setAdapter(new ArrayAdapter(OfflineRegionsActivity.this, android.R.layout.simple_list_item_1, offlineInfo){
                        @NonNull
                        @Override
                        public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                            if (convertView == null) {
                                convertView = getLayoutInflater().inflate(android.R.layout.simple_list_item_1, parent, false);
                            }

                            final TextView textView = (TextView) convertView;
                            textView.setText(offlineInfo[position]);
                            textView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (!activated) {
                                        offlineRegions[position].setDownloadState(OfflineRegion.STATE_ACTIVE);
                                    } else {
                                        offlineRegions[position].setDownloadState(OfflineRegion.STATE_INACTIVE);
                                    }

                                    activated = !activated;

                                    //Register a listener to update the list item
                                    if (currentText.isEmpty()) {
                                        currentText = textView.getText().toString();
                                    }

                                    offlineRegions[position].setObserver(new OfflineRegion.OfflineRegionObserver() {
                                        @Override
                                        public void onStatusChanged(OfflineRegionStatus status) {
                                            double progress = (100.0 * status.getCompletedResourceCount()) / status.getRequiredResourceCount();

                                            textView.setText(currentText + "\nPROGRESS : " + progress + "%");
                                        }

                                        @Override
                                        public void onError(OfflineRegionError error) {
                                            textView.setText(currentText + "\nError : " + error.getReason() + "\n" + error.getMessage());
                                        }

                                        @Override
                                        public void mapboxTileCountLimitExceeded(long limit) {
                                            textView.setText(currentText + "\nMapbox tile limit count exceeded");
                                        }
                                    });

                                }
                            });

                            return textView;
                        }
                    });

                    /*if (remainingCallbacks == 1) {
                        listView.setAdapter(new ArrayAdapter(MainActivity.this, android.R.layout.simple_list_item_1, offlineInfo));
                    }*/
                }

                @Override
                public void onError(String error) {
                    offlineInfo[position] += "\nDOWNLOADED STATUS: GET ERROR - " + error;
                    Log.e(TAG, "OFFLINE REGION STATUS : " + error);
                }
            });

            offlineRegionInfo += "\nID: " + offlineRegions[position].getID();
            offlineInfo[position] = offlineRegionInfo;
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
