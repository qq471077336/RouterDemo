package com.lwd.router_api;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;

/**
 * @AUTHOR lianwd
 * @TIME 3/8/21
 * @DESCRIPTION TODO
 */
public class BundleManager {

    private Bundle bundle = new Bundle();

    public Bundle getBundle() {
        return bundle;
    }

    public BundleManager withInt(String key, int value) {
        bundle.putInt(key, value);
        return this;
    }

    public BundleManager withBoolean(String key, boolean value) {
        bundle.putBoolean(key, value);
        return this;
    }

    public BundleManager withString(String key, String value) {
        bundle.putString(key, value);
        return this;
    }

    public BundleManager withBundle(String key, Bundle bundle) {
        bundle.putBundle(key, bundle);
        return this;
    }

    public Object navigation(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            return RouterManager.getInstance().navigation(context, this);
        }
        return null;
    }
}
