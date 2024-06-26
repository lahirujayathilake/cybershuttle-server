package org.apache.cybershuttle.api;

import org.apache.cybershuttle.handler.AgentCommunicationHandler;
import org.apache.cybershuttle.handler.ApplicationHandler;
import org.apache.cybershuttle.holder.DeferredResultHolder;
import org.apache.cybershuttle.model.PortAllocation;
import org.apache.cybershuttle.model.application.ApplicationConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private final static Logger LOGGER = LoggerFactory.getLogger(ApplicationController.class);

    private final ApplicationHandler applicationHandler;
    private final AgentCommunicationHandler agentCommunicationHandler;
    private final DeferredResultHolder deferredResultHolder;

    @Value("${cybershuttle.application.timeout}")
    private long timeout;

    public ApplicationController(ApplicationHandler applicationHandler, DeferredResultHolder deferredResultHolder,
                                 AgentCommunicationHandler agentCommunicationHandler) {
        this.applicationHandler = applicationHandler;
        this.deferredResultHolder = deferredResultHolder;
        this.agentCommunicationHandler = agentCommunicationHandler;
    }

    @GetMapping("/status")
    public String getStatus() {
        return "Application is running";
    }

    @GetMapping("/agent/{processId}")
    public ResponseEntity<AgentInfoResponse> getAgentInfo(@PathVariable("processId") String processId) {
        return ResponseEntity.accepted().body(agentCommunicationHandler.isAgentUp(processId));
    }

    @PostMapping("/agent/execute")
    public ResponseEntity<AgentCommandAck> runCommandOnAgent(@Valid @RequestBody AgentCommandRequest commandRequest) {
        LOGGER.info("Received command request to run on process {}", commandRequest.getProcessId());
        if (agentCommunicationHandler.isAgentUp(commandRequest.getProcessId()).isAgentUp()) {
            return ResponseEntity.accepted().body(agentCommunicationHandler.runCommandOnAgent(commandRequest));
        } else {
            LOGGER.warn("No agent is available to run on process {}", commandRequest.getProcessId());
            AgentCommandAck ack = new AgentCommandAck();
            ack.setError("Agent not found");
            return ResponseEntity.accepted().body(ack);
        }
    }

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

    @PostMapping("/{appId}/connect/info")
    public ResponseEntity<AppConnectInfoResponse> initiateAgentConnection(@PathVariable("appId") String appId) {
        ApplicationConfig appConfig = applicationHandler.initiateAgentConnection(appId);
        return ResponseEntity.ok().body(new AppConnectInfoResponse(appConfig.getPortAllocations().stream().findFirst().get().getPort()));
    }
}
