package org.apache.cybershuttle.api;

import java.util.List;

public class LaunchApplicationResponse {

    private String applicationId;
    private String expId;
    private List<Integer> allocatedPorts;

    public LaunchApplicationResponse(String applicationId, String expId, List<Integer> allocatedPorts) {
        this.applicationId = applicationId;
        this.expId = expId;
        this.allocatedPorts = allocatedPorts;
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

    public List<Integer> getAllocatedPorts() {
        return allocatedPorts;
    }

    public void setAllocatedPorts(List<Integer> allocatedPorts) {
        this.allocatedPorts = allocatedPorts;
    }
}
