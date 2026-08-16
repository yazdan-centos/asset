package org.mapnaom.asset.repository;

import org.mapnaom.asset.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    Optional<Project> findByCode(String code);
}
