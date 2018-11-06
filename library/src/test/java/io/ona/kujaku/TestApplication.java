package io.ona.kujaku;

import org.json.JSONObject;

public class TestApplication extends BaseKujakuApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        setTheme(R.style.AppTheme); //or just R.style.Theme_AppCompat
    }

    public void processFeatureJSON(JSONObject featureJSON) {

    }
}