package org.mapnaom.asset.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.mapnaom.asset.dto.ReferenceDtos.LocationRequest;
import org.mapnaom.asset.dto.ReferenceDtos.LocationResponse;
import org.mapnaom.asset.dto.ReferenceDtos.NamedRequest;
import org.mapnaom.asset.dto.ReferenceDtos.NamedResponse;
import org.mapnaom.asset.dto.ReferenceDtos.PersonRequest;
import org.mapnaom.asset.dto.ReferenceDtos.PersonResponse;
import org.mapnaom.asset.dto.ImportResult;
import org.mapnaom.asset.service.ReferenceDataService;
import org.mapnaom.asset.service.ReferenceExcelService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReferenceDataController {

    private final ReferenceDataService service;
    private final ReferenceExcelService excelService;

    @GetMapping("/cost-centers")
    public List<NamedResponse> costCenters() { return service.findAllCostCenters(); }

    @GetMapping("/cost-centers/{id}")
    public NamedResponse costCenter(@PathVariable Long id) { return service.findCostCenter(id); }

    @PostMapping("/cost-centers")
    public ResponseEntity<NamedResponse> createCostCenter(@Valid @RequestBody NamedRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createCostCenter(request));
    }

    @PutMapping("/cost-centers/{id}")
    public NamedResponse updateCostCenter(@PathVariable Long id, @Valid @RequestBody NamedRequest request) {
        return service.updateCostCenter(id, request);
    }

    @DeleteMapping("/cost-centers/{id}")
    public ResponseEntity<Void> deleteCostCenter(@PathVariable Long id) {
        service.deleteCostCenter(id); return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/cost-centers/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ImportResult importCostCenters(@RequestPart("file") MultipartFile file) {
        return excelService.importCostCenters(file);
    }

    @GetMapping("/projects")
    public List<NamedResponse> projects() { return service.findAllProjects(); }

    @GetMapping("/projects/{id}")
    public NamedResponse project(@PathVariable Long id) { return service.findProject(id); }

    @PostMapping("/projects")
    public ResponseEntity<NamedResponse> createProject(@Valid @RequestBody NamedRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createProject(request));
    }

    @PutMapping("/projects/{id}")
    public NamedResponse updateProject(@PathVariable Long id, @Valid @RequestBody NamedRequest request) {
        return service.updateProject(id, request);
    }

    @DeleteMapping("/projects/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        service.deleteProject(id); return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/projects/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ImportResult importProjects(@RequestPart("file") MultipartFile file) {
        return excelService.importProjects(file);
    }

    @GetMapping("/locations")
    public List<LocationResponse> locations() { return service.findAllLocations(); }

    @GetMapping("/locations/{id}")
    public LocationResponse location(@PathVariable Long id) { return service.findLocation(id); }

    @PostMapping("/locations")
    public ResponseEntity<LocationResponse> createLocation(@Valid @RequestBody LocationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createLocation(request));
    }

    @PutMapping("/locations/{id}")
    public LocationResponse updateLocation(@PathVariable Long id, @Valid @RequestBody LocationRequest request) {
        return service.updateLocation(id, request);
    }

    @DeleteMapping("/locations/{id}")
    public ResponseEntity<Void> deleteLocation(@PathVariable Long id) {
        service.deleteLocation(id); return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/locations/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ImportResult importLocations(@RequestPart("file") MultipartFile file) {
        return excelService.importLocations(file);
    }

    @PostMapping(value = "/people/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ImportResult importPeople(@RequestPart("file") MultipartFile file) {
        return excelService.importPeople(file);
    }

    @GetMapping("/people")
    public List<PersonResponse> people() { return service.findAllPeople(); }

    @GetMapping("/people/{id}")
    public PersonResponse person(@PathVariable Long id) { return service.findPerson(id); }

    @PostMapping("/people")
    public ResponseEntity<PersonResponse> createPerson(@Valid @RequestBody PersonRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createPerson(request));
    }

    @PutMapping("/people/{id}")
    public PersonResponse updatePerson(@PathVariable Long id, @Valid @RequestBody PersonRequest request) {
        return service.updatePerson(id, request);
    }

    @DeleteMapping("/people/{id}")
    public ResponseEntity<Void> deletePerson(@PathVariable Long id) {
        service.deletePerson(id); return ResponseEntity.noContent().build();
    }
}
