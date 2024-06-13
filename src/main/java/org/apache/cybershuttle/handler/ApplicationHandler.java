package org.apache.cybershuttle.handler;

import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.cybershuttle.model.application.ApplicationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ApplicationHandler {

    private final static Logger LOGGER = LoggerFactory.getLogger(ApplicationHandler.class);

    private final ExperimentHandler experimentHandler;

    public ApplicationHandler(ExperimentHandler experimentHandler) {
        this.experimentHandler = experimentHandler;
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
        return experimentHandler.createAndLaunchExperiment(relatedExp.getGatewayId(), appExperiment);
    }

}
