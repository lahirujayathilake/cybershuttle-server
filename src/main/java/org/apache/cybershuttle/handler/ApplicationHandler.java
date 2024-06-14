package org.apache.cybershuttle.handler;

import jakarta.persistence.EntityNotFoundException;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.cybershuttle.model.application.ApplicationConfig;
import org.apache.cybershuttle.model.application.ApplicationType;
import org.apache.cybershuttle.repo.ApplicationConfigRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ApplicationHandler {

    private final static Logger LOGGER = LoggerFactory.getLogger(ApplicationHandler.class);

    private final ExperimentHandler experimentHandler;
    private final ApplicationConfigRepository applicationConfigRepository;
    private final PortAllocationService portAllocationService;

    public ApplicationHandler(ExperimentHandler experimentHandler, ApplicationConfigRepository applicationConfigRepository, PortAllocationService portAllocationService) {
        this.experimentHandler = experimentHandler;
        this.applicationConfigRepository = applicationConfigRepository;
        this.portAllocationService = portAllocationService;
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

        ExperimentModel appExperiment = applicationType.getGeneratorSupplier().get().generateExperiment(relatedExp);
        String appExpId = experimentHandler.createAndLaunchExperiment(relatedExp.getGatewayId(), appExperiment);

        applicationConfigRepository.save(new ApplicationConfig(appExpId, applicationType));

        return appExpId;
    }

    public int allocatePort(String applicationExpId) {
        return portAllocationService.allocatePort(findAppConfig(applicationExpId));
    }

    public void releasePort(String applicationExpId) {
        portAllocationService.releasePort(findAppConfig(applicationExpId));
    }

    private ApplicationConfig findAppConfig(String applicationExpId) {
        return applicationConfigRepository.getByExpId(applicationExpId)
                .orElseThrow(() -> {
                    LOGGER.error("Could not find an application with the id: {}", applicationExpId);
                    return new EntityNotFoundException("Could not find an application with the id: " + applicationExpId);
                });
    }

}
