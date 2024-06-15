package org.apache.cybershuttle.api;

public class AppConnectInfoResponse {

    private int port;

    public AppConnectInfoResponse(int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
