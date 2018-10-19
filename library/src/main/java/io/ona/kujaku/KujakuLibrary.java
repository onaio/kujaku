package io.ona.kujaku;

import org.json.JSONObject;

/**
 * @author Vincent Karuri
 */
public class KujakuLibrary {

    private BaseKujakuApplication hostApplication;
    private static KujakuLibrary library;

    private KujakuLibrary() {}

    public static KujakuLibrary getInstance() {
        if (library == null) {
            library = new KujakuLibrary();
        }
        return library;
    }

    public void sendFeatureJSONToHostApp(JSONObject featureJSON) {
        getHostApplication().processFeatureJSON(featureJSON);
    }

    public void sendFeatureJSONToGeoWidget(JSONObject featureJSON) {
        // TODO: implement this
    }

    public void setHostApplication(BaseKujakuApplication hostApplication) { this.hostApplication = hostApplication; }

    public BaseKujakuApplication getHostApplication() {
        return hostApplication;
    }
}
