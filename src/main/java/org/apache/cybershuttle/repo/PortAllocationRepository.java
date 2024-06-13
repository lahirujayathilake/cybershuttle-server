package org.apache.cybershuttle.repo;

import org.apache.cybershuttle.model.PortAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PortAllocationRepository extends JpaRepository<PortAllocation, Integer> {

    boolean existsByPort(int port);

    Optional<PortAllocation> findByApplicationConfigExpId(String expId);
}
