package org.apache.cybershuttle.api;

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

import javax.validation.Valid;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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


//    @PostMapping("/async/launch")
//    public DeferredResult<LaunchApplicationResponse> launchApplication(@Valid @RequestBody LaunchApplicationRequest request) {
//        ApplicationConfig existingApp = applicationHandler.checkForLaunchedApplication(request.getApplication(), request.getExpId());
//        if (existingApp != null) {
//            if (existingApp.getPortAllocations() != null && !existingApp.getPortAllocations().isEmpty()) {
//                // No existing deferred result but existing application means returning the existing port allocations
//                DeferredResult<LaunchApplicationResponse> deferredResult = new DeferredResult<>(timeout);
//                deferredResult.setResult(new LaunchApplicationResponse(existingApp.getId(), existingApp.getRelatedExpId(), existingApp.getPortAllocations().stream().map(PortAllocation::getPort).collect(Collectors.toList())));
//                return deferredResult;
//
//            } else {
//                DeferredResult<LaunchApplicationResponse> existingDeferredResult = deferredResultHolder.get(existingApp.getId());
//                if (existingDeferredResult != null) {
//                    return existingDeferredResult;
//                }
//                // this has timed-out and agent hasn't (yet) initiated for a connection
//                applicationHandler.terminateApplication(existingApp);
//            }
//        }
//
//        String applicationId = applicationHandler.launchApplication(request.getApplication(), request.getExpId());
//
//        DeferredResult<LaunchApplicationResponse> deferredResult = new DeferredResult<>(timeout);
//        deferredResultHolder.put(applicationId, deferredResult);
//
//        deferredResult.onTimeout(() -> {
//            deferredResultHolder.remove(applicationId);
//            deferredResult.setErrorResult("Request timed out to launch the application: " + applicationHandler);
//        });
//
//        deferredResult.onCompletion(() -> deferredResultHolder.remove(applicationId));
//        return deferredResult;
//    }

    @PostMapping("/launch")
    public ResponseEntity<LaunchApplicationResponse> launchApplication(@Valid @RequestBody LaunchApplicationRequest request) {
        String applicationId = applicationHandler.launchApplication(request.getApplication(), request.getExpId(), request.getWallTimeLimit());
        return ResponseEntity.accepted().body(new LaunchApplicationResponse(applicationId, request.getExpId(), ApplicationConfig.Status.PENDING.name()));
    }

    @PostMapping("/{appId}/connect")
    public ResponseEntity<LaunchApplicationResponse> connectApplication(@PathVariable("appId") String appId) {
        ApplicationConfig existingApp = applicationHandler.findAppConfig(appId);
        ResponseEntity<LaunchApplicationResponse> response;
        Optional<Set<PortAllocation>> portAllocations = Optional.ofNullable(existingApp.getPortAllocations());

        switch (existingApp.getStatus()) {
            case COMPLETED -> response = portAllocations.filter(Predicate.not(Set::isEmpty))
                    .map(portSet -> ResponseEntity.ok(new LaunchApplicationResponse(
                            existingApp.getId(),
                            existingApp.getRelatedExpId(),
                            ApplicationConfig.Status.COMPLETED.name(),
                            portSet.stream().map(PortAllocation::getPort).collect(Collectors.toList()))))
                    .orElseGet(() -> {
                        // ApplicationConfig should not be in this state, therefore terminating it if running
                        try {
                            applicationHandler.terminateApplication(appId);
                        } catch (Exception ignored) {
                            // ignore
                        }
                        return ResponseEntity.accepted().body(new LaunchApplicationResponse(
                                existingApp.getId(),
                                existingApp.getRelatedExpId(),
                                ApplicationConfig.Status.TERMINATED.name()));
                    });
            case PENDING -> response = ResponseEntity.accepted().body(new LaunchApplicationResponse(
                    existingApp.getId(),
                    existingApp.getRelatedExpId(),
                    ApplicationConfig.Status.PENDING.name()));
            default -> response = ResponseEntity.accepted().body(new LaunchApplicationResponse(
                    existingApp.getId(),
                    existingApp.getRelatedExpId(),
                    ApplicationConfig.Status.TERMINATED.name()));
        }
        return response;
    }

    @PostMapping("/{appId}/terminate")
    public ResponseEntity<String> terminateApplication(@PathVariable("appId") String appId) {
        applicationHandler.terminateApplication(appId);
        return ResponseEntity.ok().body("Terminated the application with the Id: " + appId);
    }

//    @PostMapping("/{appId}/connect")
//    public ResponseEntity<AppConnectInfoResponse> initiateApplicationConnection(@PathVariable("appId") String appId) {
//        // TODO - handle for multiple port allocations
//        DeferredResult<LaunchApplicationResponse> deferredResult = deferredResultHolder.get(appId);
//
//        if (deferredResult != null) {
//            PortAllocation portAllocation = applicationHandler.allocatePort(appId);
//            deferredResult.setResult(new LaunchApplicationResponse(portAllocation.getApplicationConfig().getExpId(),
//                    portAllocation.getApplicationConfig().getRelatedExpId(),
//                    Stream.of(portAllocation.getPort()).toList()));
//            return ResponseEntity.ok().body(new AppConnectInfoResponse(portAllocation.getPort()));
//        }
//
//        ApplicationConfig appConfig;
//        try {
//            appConfig = applicationHandler.findAppConfig(appId);
//        } catch (EntityNotFoundException e) {
//            throw new EntityNotFoundException("No pending request found for experiment ID: " + appId, e);
//        }
//
//        if (appConfig.getPortAllocations() == null || appConfig.getPortAllocations().isEmpty()) {
//            PortAllocation portAllocation = applicationHandler.allocatePort(appId);
//            return ResponseEntity.ok().body(new AppConnectInfoResponse(portAllocation.getPort()));
//        }
//
//        return ResponseEntity.ok().body(new AppConnectInfoResponse(appConfig.getPortAllocations().stream().findFirst().get().getPort()));
//    }

    @PostMapping("/{appId}/connect/info")
    public ResponseEntity<AppConnectInfoResponse> initiateAgentConnection(@PathVariable("appId") String appId) {
        ApplicationConfig appConfig = applicationHandler.initiateAgentConnection(appId);
        return ResponseEntity.ok().body(new AppConnectInfoResponse(appConfig.getPortAllocations().stream().findFirst().get().getPort()));
    }
}
