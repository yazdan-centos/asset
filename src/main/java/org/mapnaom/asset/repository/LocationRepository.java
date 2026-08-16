package org.mapnaom.asset.repository;

import org.mapnaom.asset.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LocationRepository extends JpaRepository<Location, Long> {

    Optional<Location> findByCode(String code);
}
