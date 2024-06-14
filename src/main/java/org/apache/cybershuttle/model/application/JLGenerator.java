package org.apache.cybershuttle.model.application;

import org.apache.airavata.model.experiment.ExperimentModel;

public class JLGenerator extends ExperimentGenerator {

    @Override
    protected void customizeExperimentModel(ExperimentModel model) {
        // TODO similar to vnc password - random hash to be used as the password
    }

    @Override
    protected ApplicationType getApplicationType() {
        return ApplicationType.JUPYTER_LAB;
    }
}
