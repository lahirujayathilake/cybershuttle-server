package org.apache.cybershuttle.holder;

import org.springframework.stereotype.Component;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class DeferredResultHolder {

    private final ConcurrentHashMap<String, DeferredResult<String>> resultMap = new ConcurrentHashMap<>();

    public void put(String key, DeferredResult<String> result) {
        resultMap.put(key, result);
    }

    public DeferredResult<String> get(String key) {
        return resultMap.get(key);
    }

    public DeferredResult<String> remove(String key) {
        return resultMap.remove(key);
    }
}
