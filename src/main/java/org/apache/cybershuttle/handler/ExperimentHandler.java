package org.apache.cybershuttle.handler;

import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.cybershuttle.config.UserContext;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ExperimentHandler {

    private final static Logger LOGGER = LoggerFactory.getLogger(ExperimentHandler.class);
    private final AiravataService airavataService;

    public ExperimentHandler(AiravataService airavataService) {
        this.airavataService = airavataService;
    }

    public ExperimentModel getExperiment(String experimentId) {
        try {
            return airavataService.airavata().getExperiment(UserContext.authzToken(), experimentId);
        } catch (TException e) {
            LOGGER.error("Error while extracting the experiment with the id: {}", experimentId);
            throw new RuntimeException("Error while extracting the experiment with the id: " + experimentId, e);
        }
    }

    public String createAndLaunchExperiment(String gatewayId, ExperimentModel experiment) {
        try {
            String experimentId = airavataService.airavata().createExperiment(UserContext.authzToken(), gatewayId, experiment);
            airavataService.airavata().launchExperiment(UserContext.authzToken(), experimentId, gatewayId);
            return experimentId;
        } catch (TException e) {
            LOGGER.error("Error while creating the experiment with the name: {}", experiment.getExperimentName());
            throw new RuntimeException("Error while creating the experiment with the name: " + experiment.getExperimentName(), e);
        }
    }
}
