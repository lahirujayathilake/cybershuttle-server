package org.apache.cybershuttle.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.apache.cybershuttle.model.application.ApplicationConfig;

@Entity
@Table(name = "port_allocation")
public class PortAllocation {

    @Id
    @Column(name = "port", nullable = false)
    private Integer port;

    @ManyToOne
    @JoinColumn(name = "exp_id", referencedColumnName = "exp_id")
    private ApplicationConfig applicationConfig;

    public PortAllocation() {
    }

    public PortAllocation(Integer port, ApplicationConfig applicationConfig) {
        this.port = port;
        this.applicationConfig = applicationConfig;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public ApplicationConfig getApplicationConfig() {
        return applicationConfig;
    }

    public void setApplicationConfig(ApplicationConfig applicationConfig) {
        this.applicationConfig = applicationConfig;
    }
}
