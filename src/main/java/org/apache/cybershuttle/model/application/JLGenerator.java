package org.apache.cybershuttle.model.application;

import org.apache.airavata.model.experiment.ExperimentModel;

public class JLGenerator extends ExperimentGenerator {

    @Override
    protected void customizeExperimentModel(ExperimentModel model) {

    }

    @Override
    protected ApplicationType getApplicationType() {
        return ApplicationType.JUPYTER_LAB;
    }
}
