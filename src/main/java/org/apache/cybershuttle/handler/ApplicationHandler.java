package org.apache.cybershuttle.handler;

import jakarta.persistence.EntityNotFoundException;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.cybershuttle.model.PortAllocation;
import org.apache.cybershuttle.model.application.ApplicationConfig;
import org.apache.cybershuttle.model.application.ApplicationType;
import org.apache.cybershuttle.model.exception.InternalServerException;
import org.apache.cybershuttle.repo.ApplicationConfigRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

@Service
public class ApplicationHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationHandler.class);
    private static final String WS_HOST = "18.217.79.150";

    private final ExperimentHandler experimentHandler;
    private final ApplicationConfigRepository applicationConfigRepository;
    private final PortAllocationService portAllocationService;
    private final Environment env;

    public ApplicationHandler(ExperimentHandler experimentHandler, ApplicationConfigRepository applicationConfigRepository, PortAllocationService portAllocationService, Environment env) {
        this.experimentHandler = experimentHandler;
        this.applicationConfigRepository = applicationConfigRepository;
        this.portAllocationService = portAllocationService;
        this.env = env;
    }

    public String launchApplication(ApplicationType applicationType, String relatedExpId, int wallTimeLimit) {
        ExperimentModel relatedExp = experimentHandler.getExperiment(relatedExpId);
        if (relatedExp == null) {
            LOGGER.error("Experiment: {} cannot be null", relatedExpId);
            throw new IllegalArgumentException("Experiment: " + relatedExpId + " cannot be null");
        }

        if (applicationType == null) {
            LOGGER.error("Invalid application type: null");
            throw new IllegalArgumentException("Invalid application type: null");
        }

        if (relatedExp.getProcesses() == null || relatedExp.getProcesses().isEmpty()) {
            LOGGER.error("Related experiment: {} doesn't have a process model", relatedExpId);
            throw new IllegalArgumentException("Related experiment: " + relatedExpId + " doesn't have a process model");
        }

        ExperimentModel appExperiment = applicationType.getGeneratorSupplier().get().generateExperiment(relatedExp, getApplicationInterfaceId(applicationType), wallTimeLimit);
        String applicationId = appExperiment.getExperimentId();
        String appExpId = experimentHandler.createAndLaunchExperiment(relatedExp.getGatewayId(), appExperiment);
        LOGGER.info("Launched {} typed application. Id: {}, Application exp Id: {}, Related Exp Id: {}", applicationType.name(), applicationId, appExpId, relatedExpId);

        applicationConfigRepository.save(new ApplicationConfig(applicationId, appExpId, relatedExpId, applicationType));

        return applicationId;
    }

    public void terminateApplication(String appId) {
        try {
            ApplicationConfig appConfig = findAppConfig(appId);
            LOGGER.info("Retrieving the experiment relevant for application: {} to terminate", appId);
            ExperimentModel experiment = experimentHandler.getExperiment(appConfig.getExpId());
            LOGGER.info("Terminating the application: {}", appId);
            experimentHandler.terminateApplication(experiment.getGatewayId(), experiment.getExperimentId());

            if (appConfig.getApplicationType() == ApplicationType.VMD && !appConfig.getPortAllocations().isEmpty()) {
                for (PortAllocation allocation : appConfig.getPortAllocations()) {
                    if (allocation.getWsPID() != null) {
                        try {
                            ProcessHandle processHandle = ProcessHandle.of(allocation.getWsPID()).orElseThrow();

                            LOGGER.info("Attempting graceful shutdown of websockify process and its children with PID: {}", allocation.getWsPID());
                            processHandle.descendants().forEach(ProcessHandle::destroy);

                            boolean terminated = processHandle.onExit().orTimeout(5, TimeUnit.SECONDS).isDone();

                            if (!terminated) {
                                // Forced Termination
                                LOGGER.warn("Websockify process did not terminate gracefully. Forcing termination.");
                                processHandle.descendants().forEach(ProcessHandle::destroyForcibly);
                                processHandle.destroyForcibly();
                            } else {
                                LOGGER.info("Websockify process terminated successfully.");
                            }
                        } catch (NoSuchElementException e) {
                            LOGGER.warn("Websockify process with PID: {} not found.", allocation.getWsPID());
                        }
                    }
                }
            }

            appConfig.setStatus(ApplicationConfig.Status.TERMINATED);
            applicationConfigRepository.save(appConfig);
            portAllocationService.releasePort(appConfig);

        } catch (Exception e) {
            LOGGER.error("Error while terminating the application with the Id: {}", appId);
            throw new InternalServerException("Error while terminating the application with the Id: " + appId, e);
        }
    }

    public void terminateApplication(ApplicationConfig applicationConfig) {
        applicationConfigRepository.delete(applicationConfig);
    }

    public PortAllocation allocatePort(String applicationId) {
        return portAllocationService.allocatePort(findAppConfig(applicationId));
    }

    public void releasePort(String applicationExpId) {
        portAllocationService.releasePort(findAppConfig(applicationExpId));
    }

    public ApplicationConfig checkForLaunchedApplication(ApplicationType applicationType, String relatedExpId) {
        return applicationConfigRepository.findByApplicationTypeAndRelatedExpId(applicationType, relatedExpId).orElse(null);
    }

    public ApplicationConfig findAppConfig(String applicationId) {
        return applicationConfigRepository.findById(applicationId).orElseThrow(() -> {
            LOGGER.error("Could not find an application with the id: {}", applicationId);
            return new EntityNotFoundException("Could not find an application with the id: " + applicationId);
        });
    }

    public ApplicationConfig initiateAgentConnection(String applicationId) {
        ApplicationConfig appConfig = findAppConfig(applicationId);
        if (appConfig.getStatus() == ApplicationConfig.Status.PENDING) {
            LOGGER.info("Initiating the connection with the Agent for the application: {}", applicationId);
            PortAllocation portAllocation = portAllocationService.allocatePort(appConfig);

            if (appConfig.getApplicationType() == ApplicationType.VMD) {
                Integer websockifyPort = portAllocation.getPort() + 100;
                LOGGER.info("Creating a websockify connection for the VNC server port: {}, WS port: {}", portAllocation.getPort(), websockifyPort);
                ProcessBuilder pb = new ProcessBuilder("websockify", "-D", "0.0.0.0:" + websockifyPort, WS_HOST + ":" + portAllocation.getPort());
                try {
                    Process process = pb.start();
                    LOGGER.info("Started the WS connection, WS port: {} for VMD: {}", websockifyPort, appConfig.getId());
                    portAllocation.setWebsocketPort(websockifyPort);
                    portAllocation.setWsPID(process.pid());
                } catch (IOException e) {
                    LOGGER.error("Error starting the WS connection for application: {} for vnc port: {}, ws port: {}", appConfig.getId(), portAllocation.getPort(), websockifyPort, e);
                }
            }

            appConfig.addPortAllocation(portAllocation);

            LOGGER.info("Allocated the port: {} for application: {}", portAllocation.getPort(), applicationId);
            appConfig.setStatus(ApplicationConfig.Status.COMPLETED);
            applicationConfigRepository.save(appConfig);

            return appConfig;

        } else if (appConfig.getStatus() == ApplicationConfig.Status.COMPLETED) {
            return appConfig;
        }

        LOGGER.error("No pending application: {} found to initiate an Agent connection", applicationId);
        throw new IllegalArgumentException("No pending application:" + applicationId + " found to initiate an Agent connection");
    }

    private String getApplicationInterfaceId(ApplicationType applicationType) {
        return env.getProperty("cybershuttle.application.interface.id." + applicationType.name().toLowerCase());
    }

}
