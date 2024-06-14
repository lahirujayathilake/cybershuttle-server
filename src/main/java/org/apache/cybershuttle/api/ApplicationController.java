package org.apache.cybershuttle.api;

import org.apache.cybershuttle.handler.ApplicationHandler;
import org.apache.cybershuttle.holder.DeferredResultHolder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/application")
public class ApplicationController {

    private final ApplicationHandler applicationHandler;
    private final DeferredResultHolder deferredResultHolder;

    @Value("${cybershuttle.application.timeout}")
    private long timeout;

    public ApplicationController(ApplicationHandler applicationHandler, DeferredResultHolder deferredResultHolder) {
        this.applicationHandler = applicationHandler;
        this.deferredResultHolder = deferredResultHolder;
    }

    @GetMapping("/status")
    public String getStatus() {
        return "Application is running";
    }


    @PostMapping("/launch")
    public DeferredResult<String> launchApplication(@Valid @RequestBody LaunchApplicationRequest request) {
        String applicationExpId = applicationHandler.launchApplication(request.getApplication(), request.getExpId());

        DeferredResult<String> deferredResult = new DeferredResult<>(timeout);
        deferredResultHolder.put(applicationExpId, deferredResult);

        deferredResult.onTimeout(() -> {
            deferredResultHolder.remove(applicationExpId);
            deferredResult.setErrorResult("Request timed out to launch the application: " + applicationHandler);
        });

        deferredResult.onCompletion(() -> deferredResultHolder.remove(applicationExpId));
        return deferredResult;
    }

    @PostMapping("/{expId}/allocate-port")
    public ResponseEntity<String> allocatePort(@PathVariable("expId") String expId) {
        DeferredResult<String> deferredResult = deferredResultHolder.get(expId);

        if (deferredResult == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No pending request found  for experiment ID: " + expId);
        }

        int port = applicationHandler.allocatePort(expId);
        deferredResult.setResult(String.valueOf(port));
        return ResponseEntity.ok().body(String.valueOf(port));
    }
}
