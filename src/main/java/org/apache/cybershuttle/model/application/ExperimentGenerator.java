package org.apache.cybershuttle.model.application;

import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.experiment.ExperimentType;
import org.apache.airavata.model.experiment.UserConfigurationDataModel;
import org.apache.airavata.model.job.JobModel;
import org.apache.airavata.model.task.TaskTypes;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

public abstract class ExperimentGenerator {


    protected abstract void customizeExperimentModel(ExperimentModel model);

    protected abstract ApplicationType getApplicationType();


    public final ExperimentModel generateExperiment(ExperimentModel relatedExp) {

        String workingDir = relatedExp.getProcesses().get(0).getTasks().stream()
                .filter(task -> task.getTaskType() == TaskTypes.JOB_SUBMISSION)
                .flatMap(task -> task.getJobs().stream())
                .filter(Objects::nonNull)
                .map(JobModel::getWorkingDir)
                .filter(StringUtils::isNotBlank)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Error extracting the working directory in the related experiment: " + relatedExp.getExperimentId()));

        // Create ExperimentModel with common properties
        ExperimentModel model = new ExperimentModel();
        model.setExperimentName(this.getApplicationType().name() + "-" + relatedExp.getExperimentName());
        model.setProjectId(relatedExp.getProjectId());
        model.setUserName(relatedExp.getUserName());
        model.setGatewayId(relatedExp.getGatewayId());
        model.setExperimentType(ExperimentType.SINGLE_APPLICATION);
//        model.setExecutionId("VMD-Execution"); TODO - update after the the application creation
        model.setExecutionId(relatedExp.getExecutionId());

        UserConfigurationDataModel userConfigurationData = relatedExp.getUserConfigurationData();
        userConfigurationData.getComputationalResourceScheduling().setStaticWorkingDir(workingDir);
        model.setUserConfigurationData(userConfigurationData);

        this.customizeExperimentModel(model);

        // TODO - This should be computed in the child generator
        //  eg. airavataClient.getApplicationInputs(authzToken, config.getApplicationInterfaceId());
        model.setExperimentInputs(relatedExp.getExperimentInputs());
        model.setExperimentOutputs(relatedExp.getExperimentOutputs());

        return model;
    }
}
