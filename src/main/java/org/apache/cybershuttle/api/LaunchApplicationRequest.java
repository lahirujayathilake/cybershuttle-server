package org.apache.cybershuttle.api;

import org.apache.cybershuttle.model.application.ApplicationType;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class LaunchApplicationRequest {

    @NotNull(message = "Application type is required")
    private ApplicationType application;

    @NotBlank(message = "Experiment ID is required")
    private String expId;

    public ApplicationType getApplication() {
        return application;
    }

    public void setApplication(ApplicationType application) {
        this.application = application;
    }

    public String getExpId() {
        return expId;
    }

    public void setExpId(String expId) {
        this.expId = expId;
    }
}
