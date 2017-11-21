package io.ona.kujaku.sample.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import io.ona.kujaku.helpers.MapBoxWebServiceApi;
import io.ona.kujaku.sample.BuildConfig;
import io.ona.kujaku.sample.R;
import io.ona.kujaku.utils.Constants;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button launchKujakuMap = (Button) findViewById(R.id.btn_mainActivity_launchKujakuMap);
        launchKujakuMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callLibrary();
            }
        });

        final EditText mapBoxStyleUrl = (EditText) findViewById(R.id.edt_mainActivity_mapboxStyleURL);
        mapBoxStyleUrl.setText("mapbox://styles/ona/cj9jueph7034i2rphe0gp3o6m");
        Button downloadMapBoxStyle = (Button) findViewById(R.id.btn_mainActivity_downloadMapboxStyle);
        downloadMapBoxStyle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadMapBoxStyle(mapBoxStyleUrl.getText().toString());
            }
        });

    }

    private void callLibrary() {
        Intent intent = new Intent(Constants.INTENT_ACTION_SHOW_MAP);
        intent.putExtra(Constants.PARCELABLE_KEY_MAPBOX_ACCESS_TOKEN, BuildConfig.MAPBOX_SDK_ACCESS_TOKEN);
        startActivity(intent);
    }

    private void downloadMapBoxStyle(String mapboxStyleUrl) {
        MapBoxWebServiceApi mapBoxWebServiceApi = new MapBoxWebServiceApi(this, BuildConfig.MAPBOX_SDK_ACCESS_TOKEN);
        mapBoxWebServiceApi.retrieveStyleJSON(mapboxStyleUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Toast.makeText(MainActivity.this, response, Toast.LENGTH_SHORT)
                        .show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "Error downloading MapBox Style JSON : " + error.getMessage(), Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }
}
