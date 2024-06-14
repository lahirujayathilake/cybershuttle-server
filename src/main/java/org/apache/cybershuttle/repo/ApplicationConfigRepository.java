package org.apache.cybershuttle.repo;

import org.apache.cybershuttle.model.application.ApplicationConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApplicationConfigRepository extends JpaRepository<ApplicationConfig, String> {

    Optional<ApplicationConfig> getByExpId(String expId);
}
