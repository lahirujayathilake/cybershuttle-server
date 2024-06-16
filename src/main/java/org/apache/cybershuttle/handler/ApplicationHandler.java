package org.apache.cybershuttle.handler;

import jakarta.persistence.EntityNotFoundException;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.cybershuttle.model.PortAllocation;
import org.apache.cybershuttle.model.application.ApplicationConfig;
import org.apache.cybershuttle.model.application.ApplicationType;
import org.apache.cybershuttle.repo.ApplicationConfigRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
public class ApplicationHandler {

    private final static Logger LOGGER = LoggerFactory.getLogger(ApplicationHandler.class);

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

    public String launchApplication(ApplicationType applicationType, String relatedExpId) {
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

        ExperimentModel appExperiment = applicationType.getGeneratorSupplier().get().generateExperiment(relatedExp, getApplicationInterfaceId(applicationType));
        String applicationId = appExperiment.getExperimentId();
        String appExpId = experimentHandler.createAndLaunchExperiment(relatedExp.getGatewayId(), appExperiment);
        LOGGER.info("Launch {} typed application. Id: {}, Application exp Id: {}, Related Exp Id: {}", applicationType.name(), applicationId, appExpId, relatedExpId);

        applicationConfigRepository.save(new ApplicationConfig(applicationId, appExpId, relatedExpId, applicationType));

        return applicationId;
    }

    public void terminateApplication(String appId) {
        applicationConfigRepository.delete(findAppConfig(appId));
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
        return applicationConfigRepository.findById(applicationId)
                .orElseThrow(() -> {
                    LOGGER.error("Could not find an application with the id: {}", applicationId);
                    return new EntityNotFoundException("Could not find an application with the id: " + applicationId);
                });
    }

    private String getApplicationInterfaceId(ApplicationType applicationType) {
        return env.getProperty("cybershuttle.application.interface.id." + applicationType.name().toLowerCase());
    }

}
