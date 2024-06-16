package org.apache.cybershuttle.model.application;

import org.apache.airavata.model.application.io.DataType;
import org.apache.airavata.model.application.io.InputDataObjectType;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.experiment.ExperimentType;
import org.apache.airavata.model.experiment.UserConfigurationDataModel;
import org.apache.airavata.model.job.JobModel;
import org.apache.airavata.model.task.TaskTypes;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public abstract class ExperimentGenerator {


    protected abstract void customizeExperimentModel(ExperimentModel model);

    protected abstract ApplicationType getApplicationType();


    public final ExperimentModel generateExperiment(ExperimentModel relatedExp, String executionId) {

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
        String applicationId = this.getApplicationType().name() + "-" + UUID.randomUUID();
        model.setExperimentId(applicationId);
        model.setExperimentName(this.getApplicationType().name() + "-" + relatedExp.getExperimentName());
        model.setProjectId(relatedExp.getProjectId());
        model.setUserName(relatedExp.getUserName());
        model.setGatewayId(relatedExp.getGatewayId());
        model.setExperimentType(ExperimentType.SINGLE_APPLICATION);
        model.setExecutionId(executionId);

        UserConfigurationDataModel userConfigurationData = relatedExp.getUserConfigurationData();
        userConfigurationData.getComputationalResourceScheduling().setStaticWorkingDir(workingDir);
        model.setUserConfigurationData(userConfigurationData);

        List<InputDataObjectType> applicationInputs = new ArrayList<>();

        InputDataObjectType inputWorkingDir = new InputDataObjectType();
        inputWorkingDir.setName("working_dir");
        inputWorkingDir.setType(DataType.STRING);
        inputWorkingDir.setValue(workingDir);

        InputDataObjectType inputExpId = new InputDataObjectType();
        inputExpId.setName("application_id");
        inputExpId.setType(DataType.STRING);
        inputExpId.setValue(applicationId);

        applicationInputs.add(inputWorkingDir);
        applicationInputs.add(inputExpId);
        model.setExperimentInputs(applicationInputs);

        this.customizeExperimentModel(model);

        return model;
    }
}
