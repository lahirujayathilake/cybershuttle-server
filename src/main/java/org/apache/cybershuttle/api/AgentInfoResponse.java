package org.apache.cybershuttle.api;

public class AgentInfoResponse {
    private String processId;
    private boolean isAgentUp;

    public AgentInfoResponse(String processId, boolean isAgentUp) {
        this.processId = processId;
        this.isAgentUp = isAgentUp;
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public boolean isAgentUp() {
        return isAgentUp;
    }

    public void setAgentUp(boolean agentUp) {
        isAgentUp = agentUp;
    }
}
