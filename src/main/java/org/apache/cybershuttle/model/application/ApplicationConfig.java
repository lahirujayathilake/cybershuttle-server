package org.apache.cybershuttle.model.application;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import org.apache.cybershuttle.model.PortAllocation;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "app_config")
public class ApplicationConfig {

    @Id
    @Column(name = "exp_id", nullable = false)
    private String expId;
    
    @Column(name = "related_exp_id", nullable = false)
    private String relatedExpId;

    @Enumerated(EnumType.STRING)
    @Column(name = "application_type", nullable = false)
    private ApplicationType applicationType;

    @OneToMany(mappedBy = "applicationConfig", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PortAllocation> portAllocations = new HashSet<>();

    private long createdAt;

    public ApplicationConfig() {
    }

    public ApplicationConfig(String expId, String relatedExpId, ApplicationType applicationType) {
        this.expId = expId;
        this.relatedExpId = relatedExpId;
        this.applicationType = applicationType;
    }

    private void onCreate() {
        createdAt = Instant.now().toEpochMilli();
    }

    public String getExpId() {
        return expId;
    }

    public String getRelatedExpId() {
        return relatedExpId;
    }

    public void setRelatedExpId(String relatedExpId) {
        this.relatedExpId = relatedExpId;
    }

    public Set<PortAllocation> getPortAllocations() {
        return portAllocations;
    }

    public void setPortAllocations(Set<PortAllocation> portAllocations) {
        this.portAllocations = portAllocations;
    }

    public void setExpId(String expId) {
        this.expId = expId;
    }

    public ApplicationType getApplicationType() {
        return applicationType;
    }

    public void setApplicationType(ApplicationType applicationType) {
        this.applicationType = applicationType;
    }

    public void addPortAllocation(PortAllocation portAllocation) {
        portAllocations.add(portAllocation);
        portAllocation.setApplicationConfig(this);
    }

    public void removePortAllocation(PortAllocation portAllocation) {
        portAllocations.remove(portAllocation);
        portAllocation.setApplicationConfig(null);
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
