package io.ona.kujaku.listeners;

import io.ona.kujaku.wmts.model.WmtsCapabilities;

/**
 * Listener called when capabilities have been retrieved
 *
 * Created by Emmanuel Otin - eo@novel-t.ch 11/28/18.
 */
public interface WmtsCapabilitiesListener {
    void onCapabilitiesReceived(WmtsCapabilities capabilities);
    void onCapabilitiesError(Exception ex);
}
