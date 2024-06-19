package org.apache.cybershuttle.handler;

import jakarta.annotation.PostConstruct;
import org.apache.cybershuttle.model.PortAllocation;
import org.apache.cybershuttle.model.PortRange;
import org.apache.cybershuttle.model.application.ApplicationConfig;
import org.apache.cybershuttle.model.exception.NoAvailablePortException;
import org.apache.cybershuttle.repo.PortAllocationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PortAllocationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PortAllocationService.class);

    private final PortAllocationRepository portAllocationRepository;
    private final Object lock = new Object();
    private final Set<Integer> allocatedPortsCache = Collections.newSetFromMap(new ConcurrentHashMap<>());

    @Value("${cybershuttle.server.port.ranges}")
    private String portRangesString;
    private List<PortRange> availablePortRanges;


    public PortAllocationService(PortAllocationRepository portAllocationRepository) {
        this.portAllocationRepository = portAllocationRepository;
    }

    @PostConstruct
    public void initializePortRanges() {
        availablePortRanges = parsePortRanges(portRangesString);
    }

    public PortAllocation allocatePort(ApplicationConfig applicationConfig) {
        // TODO extend the method for multiple port allocations based on the application type
        synchronized (lock) {
            for (PortRange range : availablePortRanges) {
                for (int port = range.getStartPort(); port <= range.getEndPort(); port++) {
                    if (!isPortAllocated(port)) {
                        PortAllocation allocation = new PortAllocation(port, applicationConfig);
                        allocatedPortsCache.add(port);
                        return allocation;
                    }
                }
            }
        }

        throw new NoAvailablePortException("No available port found within the configured ranges");
    }

    public void releasePort(ApplicationConfig applicationConfig) {
        for (PortAllocation allocation : applicationConfig.getPortAllocations()) {
            portAllocationRepository.delete(allocation);
            allocatedPortsCache.remove(allocation.getPort());
        }
    }


    private boolean isPortAllocated(int port) {
        if (allocatedPortsCache.contains(port)) {
            return true;
        } else {
            boolean isAllocated = portAllocationRepository.existsByPort(port);
            if (isAllocated) {
                allocatedPortsCache.add(port);
            }
            return isAllocated;
        }
    }

    private List<PortRange> parsePortRanges(String portRangesString) {
        List<PortRange> portRanges = new ArrayList<>();
        for (String rangeStr : portRangesString.split(",")) {
            String[] parts = rangeStr.trim().split("-");
            int startPort = Integer.parseInt(parts[0]);
            int endPort = parts.length > 1 ? Integer.parseInt(parts[1]) : startPort;
            portRanges.add(new PortRange(startPort, endPort));
        }
        return portRanges;
    }
}
