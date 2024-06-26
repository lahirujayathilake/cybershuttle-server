package org.apache.cybershuttle.api;

import java.util.ArrayList;
import java.util.List;

public class AgentCommandRequest {
    private List<String> arguments = new ArrayList<>();
    private String workingDir;
    private String processId;

    public List<String> getArguments() {
        return arguments;
    }

    public void setArguments(List<String> arguments) {
        this.arguments = arguments;
    }

    public String getWorkingDir() {
        return workingDir;
    }

    public void setWorkingDir(String workingDir) {
        this.workingDir = workingDir;
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }
}
