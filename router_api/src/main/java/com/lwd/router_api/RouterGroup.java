package com.lwd.router_api;

import java.util.Map;

/**
 * @AUTHOR lianwd
 * @TIME 3/2/21
 * @DESCRIPTION TODO
 */
public interface RouterGroup {
    Map<String, Class<? extends RouterPath>> getGroupMap();
}
