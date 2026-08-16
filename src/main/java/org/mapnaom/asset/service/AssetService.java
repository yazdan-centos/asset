package org.mapnaom.asset.service;

import lombok.RequiredArgsConstructor;
import org.mapnaom.asset.dto.AssetRequest;
import org.mapnaom.asset.dto.AssetResponse;
import org.mapnaom.asset.dto.AssetResponse.PersonValue;
import org.mapnaom.asset.dto.AssetResponse.ReferenceValue;
import org.mapnaom.asset.entity.Asset;
import org.mapnaom.asset.exception.ResourceNotFoundException;
import org.mapnaom.asset.repository.AssetRepository;
import org.mapnaom.asset.repository.CostCenterRepository;
import org.mapnaom.asset.repository.LocationRepository;
import org.mapnaom.asset.repository.PersonRepository;
import org.mapnaom.asset.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AssetService {

    private final AssetRepository assetRepository;
    private final CostCenterRepository costCenterRepository;
    private final ProjectRepository projectRepository;
    private final LocationRepository locationRepository;
    private final PersonRepository personRepository;

    public List<AssetResponse> findAll() {
        return assetRepository.findAll().stream().map(this::toResponse).toList();
    }

    public AssetResponse findById(Long id) {
        return toResponse(require(id));
    }

    @Transactional
    public AssetResponse create(AssetRequest request) {
        String plateNumber = request.plateNumber().trim();
        if (assetRepository.existsByPlateNumber(plateNumber)) {
            throw new IllegalArgumentException("Asset plate number already exists: " + plateNumber);
        }
        Asset asset = new Asset();
        apply(asset, request);
        return toResponse(assetRepository.save(asset));
    }

    @Transactional
    public AssetResponse update(Long id, AssetRequest request) {
        Asset asset = require(id);
        String plateNumber = request.plateNumber().trim();
        assetRepository.findByPlateNumber(plateNumber)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Asset plate number already exists: " + plateNumber);
                });
        apply(asset, request);
        return toResponse(assetRepository.save(asset));
    }

    @Transactional
    public void delete(Long id) {
        assetRepository.delete(require(id));
        assetRepository.flush();
    }

    @Transactional
    public boolean upsert(AssetRequest request) {
        Asset asset = assetRepository.findByPlateNumber(request.plateNumber().trim()).orElseGet(Asset::new);
        boolean created = asset.getId() == null;
        apply(asset, request);
        assetRepository.save(asset);
        return created;
    }

    private Asset require(Long id) {
        return assetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found: " + id));
    }

    private void apply(Asset asset, AssetRequest request) {
        asset.setPlateNumber(request.plateNumber().trim());
        asset.setTitle(request.title().trim());
        asset.setCommissioningDate(request.commissioningDate());
        asset.setAssetGroup(request.assetGroup());
        asset.setDepreciationMethod(request.depreciationMethod());
        asset.setCostCenter(costCenterRepository.findByCode(request.costCenterCode().trim())
                .orElseThrow(() -> new IllegalArgumentException("Unknown cost center code: " + request.costCenterCode())));
        asset.setProject(optionalCode(request.projectCode()) == null ? null
                : projectRepository.findByCode(optionalCode(request.projectCode()))
                .orElseThrow(() -> new IllegalArgumentException("Unknown project code: " + request.projectCode())));
        asset.setLocation(locationRepository.findByCode(request.locationCode().trim())
                .orElseThrow(() -> new IllegalArgumentException("Unknown location code: " + request.locationCode())));
        asset.setCustodian(personRepository.findByPersonnelCode(request.custodianPersonnelCode().trim())
                .orElseThrow(() -> new IllegalArgumentException("Unknown custodian personnel code: " + request.custodianPersonnelCode())));
        asset.setResponsiblePerson(personRepository.findByPersonnelCode(request.responsiblePersonnelCode().trim())
                .orElseThrow(() -> new IllegalArgumentException("Unknown responsible personnel code: " + request.responsiblePersonnelCode())));
        asset.setAcquisitionCost(request.acquisitionCost());
        asset.setAccumulatedDepreciation(request.accumulatedDepreciation());
        asset.setStatus(request.status());
        asset.setDepreciationStatus(request.depreciationStatus());
    }

    private AssetResponse toResponse(Asset asset) {
        ReferenceValue project = asset.getProject() == null ? null
                : new ReferenceValue(asset.getProject().getId(), asset.getProject().getCode(), asset.getProject().getName());
        return new AssetResponse(
                asset.getId(), asset.getVersion(), asset.getCreatedAt(), asset.getUpdatedAt(),
                asset.getPlateNumber(), asset.getTitle(), asset.getCommissioningDate(), asset.getAssetGroup(),
                asset.getDepreciationMethod(),
                new ReferenceValue(asset.getCostCenter().getId(), asset.getCostCenter().getCode(), asset.getCostCenter().getName()),
                project,
                new ReferenceValue(asset.getLocation().getId(), asset.getLocation().getCode(), asset.getLocation().getName()),
                new PersonValue(asset.getCustodian().getId(), asset.getCustodian().getPersonnelCode(), asset.getCustodian().getFullName()),
                new PersonValue(asset.getResponsiblePerson().getId(), asset.getResponsiblePerson().getPersonnelCode(), asset.getResponsiblePerson().getFullName()),
                asset.getAcquisitionCost(), asset.getAccumulatedDepreciation(), asset.getBookValue(),
                asset.getStatus(), asset.getDepreciationStatus()
        );
    }

    private String optionalCode(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
