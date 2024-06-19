package org.apache.cybershuttle.api;

import java.util.List;

public class LaunchApplicationResponse {

    private String applicationId;
    private String expId;
    private String status;
    private List<Integer> allocatedPorts;

    public LaunchApplicationResponse(String applicationId, String expId, List<Integer> allocatedPorts) {
        this.applicationId = applicationId;
        this.expId = expId;
        this.allocatedPorts = allocatedPorts;
    }

    public LaunchApplicationResponse(String applicationId, String expId, String status, List<Integer> allocatedPorts) {
        this(applicationId, expId, status);
        this.allocatedPorts = allocatedPorts;
    }

    public LaunchApplicationResponse(String applicationId, String expId, String status) {
        this.applicationId = applicationId;
        this.expId = expId;
        this.status = status;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getExpId() {
        return expId;
    }

    public void setExpId(String expId) {
        this.expId = expId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<Integer> getAllocatedPorts() {
        return allocatedPorts;
    }

    public void setAllocatedPorts(List<Integer> allocatedPorts) {
        this.allocatedPorts = allocatedPorts;
    }
}
