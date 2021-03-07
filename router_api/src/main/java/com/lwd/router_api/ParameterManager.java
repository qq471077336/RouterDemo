package com.lwd.router_api;

import android.app.Activity;
import android.util.LruCache;

/**
 * @AUTHOR lianwd
 * @TIME 3/7/21
 * @DESCRIPTION TODO
 */
public class ParameterManager {

    private static ParameterManager instance;
    private LruCache<String, ParameterGet> cache;

    private ParameterManager() {
        cache = new LruCache<>(100);
    }

    public static ParameterManager getInstance() {
        if (instance == null) {
            synchronized (ParameterManager.class) {
                if (instance == null) {
                    instance = new ParameterManager();
                }
            }
        }
        return instance;
    }

    static final String FILE_SUFFIX_NAME = "$$Parameter"; // 为了这个效果：MainActivity + $$Parameter

    public void loadParameter(Activity activity) {
        String name = activity.getClass().getName();
        ParameterGet parameterGet = cache.get(name);
        if (parameterGet == null) {
            Class<?> aClass = null;
            try {
                aClass = Class.forName(name + FILE_SUFFIX_NAME);
                parameterGet = (ParameterGet) aClass.newInstance();
                cache.put(name, parameterGet);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        parameterGet.getParameter(activity);
    }
}
