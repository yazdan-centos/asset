package org.mapnaom.asset.repository;

import org.mapnaom.asset.entity.Asset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface AssetRepository extends JpaRepository<Asset, Long>, JpaSpecificationExecutor<Asset> {

    Optional<Asset> findByPlateNumber(String plateNumber);

    boolean existsByPlateNumber(String plateNumber);
}
