package org.mapnaom.asset.service;

import lombok.RequiredArgsConstructor;
import org.mapnaom.asset.dto.ReferenceDtos.LocationRequest;
import org.mapnaom.asset.dto.ReferenceDtos.LocationResponse;
import org.mapnaom.asset.dto.ReferenceDtos.NamedRequest;
import org.mapnaom.asset.dto.ReferenceDtos.NamedResponse;
import org.mapnaom.asset.dto.ReferenceDtos.PersonRequest;
import org.mapnaom.asset.dto.ReferenceDtos.PersonResponse;
import org.mapnaom.asset.entity.CostCenter;
import org.mapnaom.asset.entity.Location;
import org.mapnaom.asset.entity.Person;
import org.mapnaom.asset.entity.Project;
import org.mapnaom.asset.exception.ResourceNotFoundException;
import org.mapnaom.asset.repository.CostCenterRepository;
import org.mapnaom.asset.repository.LocationRepository;
import org.mapnaom.asset.repository.PersonRepository;
import org.mapnaom.asset.repository.ProjectRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReferenceDataService {

    private final CostCenterRepository costCenterRepository;
    private final ProjectRepository projectRepository;
    private final LocationRepository locationRepository;
    private final PersonRepository personRepository;

    public List<NamedResponse> findAllCostCenters() {
        return costCenterRepository.findAll().stream().map(this::toResponse).toList();
    }

    public NamedResponse findCostCenter(Long id) {
        return toResponse(require(costCenterRepository, id, "Cost center"));
    }

    @Transactional
    public NamedResponse createCostCenter(NamedRequest request) {
        CostCenter entity = new CostCenter();
        apply(entity, request);
        return toResponse(costCenterRepository.save(entity));
    }

    @Transactional
    public NamedResponse updateCostCenter(Long id, NamedRequest request) {
        CostCenter entity = require(costCenterRepository, id, "Cost center");
        apply(entity, request);
        return toResponse(costCenterRepository.save(entity));
    }

    @Transactional
    public void deleteCostCenter(Long id) {
        delete(costCenterRepository, id, "Cost center");
    }

    public List<NamedResponse> findAllProjects() {
        return projectRepository.findAll().stream().map(this::toResponse).toList();
    }

    public NamedResponse findProject(Long id) {
        return toResponse(require(projectRepository, id, "Project"));
    }

    @Transactional
    public NamedResponse createProject(NamedRequest request) {
        Project entity = new Project();
        apply(entity, request);
        return toResponse(projectRepository.save(entity));
    }

    @Transactional
    public NamedResponse updateProject(Long id, NamedRequest request) {
        Project entity = require(projectRepository, id, "Project");
        apply(entity, request);
        return toResponse(projectRepository.save(entity));
    }

    @Transactional
    public void deleteProject(Long id) {
        delete(projectRepository, id, "Project");
    }

    public List<LocationResponse> findAllLocations() {
        return locationRepository.findAll().stream().map(this::toResponse).toList();
    }

    public LocationResponse findLocation(Long id) {
        return toResponse(require(locationRepository, id, "Location"));
    }

    @Transactional
    public LocationResponse createLocation(LocationRequest request) {
        Location entity = new Location();
        apply(entity, request);
        return toResponse(locationRepository.save(entity));
    }

    @Transactional
    public LocationResponse updateLocation(Long id, LocationRequest request) {
        Location entity = require(locationRepository, id, "Location");
        apply(entity, request);
        return toResponse(locationRepository.save(entity));
    }

    @Transactional
    public void deleteLocation(Long id) {
        delete(locationRepository, id, "Location");
    }

    public List<PersonResponse> findAllPeople() {
        return personRepository.findAll().stream().map(this::toResponse).toList();
    }

    public PersonResponse findPerson(Long id) {
        return toResponse(require(personRepository, id, "Person"));
    }

    @Transactional
    public PersonResponse createPerson(PersonRequest request) {
        Person entity = new Person();
        apply(entity, request);
        return toResponse(personRepository.save(entity));
    }

    @Transactional
    public PersonResponse updatePerson(Long id, PersonRequest request) {
        Person entity = require(personRepository, id, "Person");
        apply(entity, request);
        return toResponse(personRepository.save(entity));
    }

    @Transactional
    public void deletePerson(Long id) {
        delete(personRepository, id, "Person");
    }

    private void apply(CostCenter entity, NamedRequest request) {
        entity.setCode(request.code().trim());
        entity.setName(request.name().trim());
        entity.setActive(request.active());
    }

    private void apply(Project entity, NamedRequest request) {
        entity.setCode(request.code().trim());
        entity.setName(request.name().trim());
        entity.setActive(request.active());
    }

    private void apply(Location entity, LocationRequest request) {
        entity.setCode(request.code().trim());
        entity.setName(request.name().trim());
        entity.setAddress(trimToNull(request.address()));
        entity.setActive(request.active());
    }

    private void apply(Person entity, PersonRequest request) {
        entity.setPersonnelCode(request.personnelCode().trim());
        entity.setFullName(request.fullName().trim());
        entity.setActive(request.active());
    }

    private NamedResponse toResponse(CostCenter entity) {
        return new NamedResponse(entity.getId(), entity.getVersion(), entity.getCreatedAt(), entity.getUpdatedAt(),
                entity.getCode(), entity.getName(), entity.isActive());
    }

    private NamedResponse toResponse(Project entity) {
        return new NamedResponse(entity.getId(), entity.getVersion(), entity.getCreatedAt(), entity.getUpdatedAt(),
                entity.getCode(), entity.getName(), entity.isActive());
    }

    private LocationResponse toResponse(Location entity) {
        return new LocationResponse(entity.getId(), entity.getVersion(), entity.getCreatedAt(), entity.getUpdatedAt(),
                entity.getCode(), entity.getName(), entity.getAddress(), entity.isActive());
    }

    private PersonResponse toResponse(Person entity) {
        return new PersonResponse(entity.getId(), entity.getVersion(), entity.getCreatedAt(), entity.getUpdatedAt(),
                entity.getPersonnelCode(), entity.getFullName(), entity.isActive());
    }

    private <T> T require(JpaRepository<T, Long> repository, Long id, String label) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(label + " not found: " + id));
    }

    private <T> void delete(JpaRepository<T, Long> repository, Long id, String label) {
        T entity = require(repository, id, label);
        repository.delete(entity);
        repository.flush();
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
