package org.apache.cybershuttle.api;

import jakarta.persistence.EntityNotFoundException;
import org.apache.cybershuttle.handler.ApplicationHandler;
import org.apache.cybershuttle.holder.DeferredResultHolder;
import org.apache.cybershuttle.model.PortAllocation;
import org.apache.cybershuttle.model.application.ApplicationConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import javax.validation.Valid;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    public DeferredResult<LaunchApplicationResponse> launchApplication(@Valid @RequestBody LaunchApplicationRequest request) {
        ApplicationConfig existingApp = applicationHandler.checkForLaunchedApplication(request.getApplication(), request.getExpId());
        if (existingApp != null) {
            if (existingApp.getPortAllocations() != null && !existingApp.getPortAllocations().isEmpty()) {
                // No existing deferred result but existing application means returning the existing port allocations
                DeferredResult<LaunchApplicationResponse> deferredResult = new DeferredResult<>(timeout);
                deferredResult.setResult(new LaunchApplicationResponse(existingApp.getExpId(), existingApp.getRelatedExpId(), existingApp.getPortAllocations().stream().map(PortAllocation::getPort).collect(Collectors.toList())));
                return deferredResult;

            } else {
                DeferredResult<LaunchApplicationResponse> existingDeferredResult = deferredResultHolder.get(existingApp.getExpId());
                if (existingDeferredResult != null) {
                    return existingDeferredResult;
                }
                // this has timed-out and agent hasn't (yet) initiated for a connection
                applicationHandler.terminateApplication(existingApp);
            }
        }

        String applicationExpId = applicationHandler.launchApplication(request.getApplication(), request.getExpId());

        DeferredResult<LaunchApplicationResponse> deferredResult = new DeferredResult<>(timeout);
        deferredResultHolder.put(applicationExpId, deferredResult);

        deferredResult.onTimeout(() -> {
            deferredResultHolder.remove(applicationExpId);
            deferredResult.setErrorResult("Request timed out to launch the application: " + applicationHandler);
        });

        deferredResult.onCompletion(() -> deferredResultHolder.remove(applicationExpId));
        return deferredResult;
    }

    @PostMapping("/terminate/{expId}")
    public ResponseEntity<String> terminateApplication(@PathVariable("expId") String expId) {
        applicationHandler.terminateApplication(expId);
        return ResponseEntity.ok().body("Application released for experiment ID: " + expId);
    }

    @PostMapping("/{expId}/connect/info")
    public ResponseEntity<AppConnectInfoResponse> initiateApplicationConnection(@PathVariable("expId") String expId) {
        // TODO - handle for multiple port allocations
        DeferredResult<LaunchApplicationResponse> deferredResult = deferredResultHolder.get(expId);

        if (deferredResult != null) {
            PortAllocation portAllocation = applicationHandler.allocatePort(expId);
            deferredResult.setResult(new LaunchApplicationResponse(portAllocation.getApplicationConfig().getExpId(),
                    portAllocation.getApplicationConfig().getRelatedExpId(),
                    Stream.of(portAllocation.getPort()).toList()));
            return ResponseEntity.ok().body(new AppConnectInfoResponse(portAllocation.getPort()));
        }

        ApplicationConfig appConfig;
        try {
            appConfig = applicationHandler.findAppConfig(expId);
        } catch (EntityNotFoundException e) {
            throw new EntityNotFoundException("No pending request found for experiment ID: " + expId, e);
        }

        if (appConfig.getPortAllocations() == null || appConfig.getPortAllocations().isEmpty()) {
            PortAllocation portAllocation = applicationHandler.allocatePort(expId);
            return ResponseEntity.ok().body(new AppConnectInfoResponse(portAllocation.getPort()));
        }

        return ResponseEntity.ok().body(new AppConnectInfoResponse(appConfig.getPortAllocations().stream().findFirst().get().getPort()));
    }
}
