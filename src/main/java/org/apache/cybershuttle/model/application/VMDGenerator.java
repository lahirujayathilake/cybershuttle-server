package org.apache.cybershuttle.model.application;

import org.apache.airavata.model.experiment.ExperimentModel;

public class VMDGenerator  extends ExperimentGenerator {

    @Override
    protected void customizeExperimentModel(ExperimentModel model) {
//        model.setExperimentInputs(model.getExperimentInputs());
//        model.setExperimentOutputs(model.getExperimentOutputs());
    }

    @Override
    protected ApplicationType getApplicationType() {
        return ApplicationType.VMD;
    }
}
