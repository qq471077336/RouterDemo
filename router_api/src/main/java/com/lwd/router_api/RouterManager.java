package com.lwd.router_api;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;
import android.util.LruCache;

import androidx.annotation.RequiresApi;

import com.lwd.router_annotations.bean.RouterBean;

/**
 * @AUTHOR lianwd
 * @TIME 3/8/21
 * @DESCRIPTION TODO
 */
public class RouterManager {
    LruCache<String, RouterPath> pathCache;
    LruCache<String, RouterGroup> groupCache;

    private static RouterManager instance;

    private String path;
    private String group;

    private RouterManager() {
        pathCache = new LruCache<>(100);
        groupCache = new LruCache<>(100);
    }

    public static RouterManager getInstance() {
        if (instance == null) {
            synchronized (RouterManager.class) {
                if (instance == null) {
                    instance = new RouterManager();
                }
            }
        }
        return instance;
    }

    public BundleManager build(String path) {
        if (!check(path)) {
            throw new IllegalArgumentException("参数不合法");
        }

        return new BundleManager();
    }

    private boolean check(String path) {

        if (TextUtils.isEmpty(path) || !path.startsWith("/")) {
            return false;
        }

        if (path.lastIndexOf("/") == 0) {
            return false;
        }

        String finalGroup = path.substring(1, path.indexOf("/", 1));
        if (TextUtils.isEmpty(finalGroup)) {
            return false;
        }

        this.path = path;
        this.group = finalGroup;
        System.out.println("组名：" + group);

        return true;
    }

    private static final String GROUP_PRE_NAME = "Router$$Group$$";

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public Object navigation(Context context, BundleManager bundleManager) {
        RouterGroup routerGroup = groupCache.get(group);
        try {
            if (routerGroup == null) {
                String groupName = context.getPackageName() + "." + GROUP_PRE_NAME + group;

                Class<?> aClass = Class.forName(groupName);
                routerGroup = (RouterGroup) aClass.newInstance();
                groupCache.put(groupName, routerGroup);
            }

            if (routerGroup.getGroupMap().isEmpty()) {
                throw new RuntimeException("Group路由表无效");
            }

            RouterPath routerPath = pathCache.get(path);
            if (routerPath == null) {
                Class<? extends RouterPath> aClass = routerGroup.getGroupMap().get(group);
                routerPath = aClass.newInstance();
                pathCache.put(path, routerPath);
            }

            if (routerPath.getPathMap().isEmpty()) {
                throw new RuntimeException("Path路由表无效");
            }

            RouterBean routerBean = routerPath.getPathMap().get(path);
            if (routerBean != null) {
                switch (routerBean.getTypeEnum()) {
                    case ACTIVITY:
                        Intent intent = new Intent(context, routerBean.getMyClass());
                        intent.putExtras(bundleManager.getBundle());
                        context.startActivity(intent, bundleManager.getBundle());
                        break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
