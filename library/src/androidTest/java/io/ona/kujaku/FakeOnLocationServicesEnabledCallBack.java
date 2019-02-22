package io.ona.kujaku;

import android.location.Location;

import io.ona.kujaku.listeners.OnLocationChanged;

/**
 * @author Vincent Karuri
 */
public class FakeOnLocationServicesEnabledCallBack implements OnLocationChanged {
    boolean isCallBackWasInvoked = false;

    @Override
    public void onLocationChanged(Location location) {
        isCallBackWasInvoked = true;
    }

    public boolean getIsCallBackWasInvoke() {
        return isCallBackWasInvoked;
    }
}
