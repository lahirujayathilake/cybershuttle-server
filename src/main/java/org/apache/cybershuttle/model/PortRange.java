package org.apache.cybershuttle.model;

public class PortRange {

    private int startPort;
    private int endPort;

    public PortRange(int startPort, int endPort) {
        this.startPort = startPort;
        this.endPort = endPort;
    }

    public int getStartPort() {
        return startPort;
    }

    public void setStartPort(int startPort) {
        this.startPort = startPort;
    }

    public int getEndPort() {
        return endPort;
    }

    public void setEndPort(int endPort) {
        this.endPort = endPort;
    }
}
