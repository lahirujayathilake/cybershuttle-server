package org.apache.cybershuttle.holder;

import org.apache.cybershuttle.api.LaunchApplicationResponse;
import org.apache.cybershuttle.model.PortAllocation;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class DeferredResultHolder {

    private final ConcurrentHashMap<String, DeferredResult<LaunchApplicationResponse>> resultMap = new ConcurrentHashMap<>();

    public void put(String key, DeferredResult<LaunchApplicationResponse> result) {
        resultMap.put(key, result);
    }

    public DeferredResult<LaunchApplicationResponse> get(String key) {
        return resultMap.get(key);
    }

    public DeferredResult<LaunchApplicationResponse> remove(String key) {
        return resultMap.remove(key);
    }
}
