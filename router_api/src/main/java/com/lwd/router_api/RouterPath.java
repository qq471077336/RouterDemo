package com.lwd.router_api;

import com.lwd.router_annotations.bean.RouterBean;

import java.util.Map;

/**
 * @AUTHOR lianwd
 * @TIME 3/2/21
 * @DESCRIPTION TODO
 */
public interface RouterPath {
    Map<String, RouterBean> getPathMap();
}
