package org.mapnaom.asset.service;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.mapnaom.asset.dto.ImportResult;
import org.mapnaom.asset.dto.ReferenceDtos.LocationRequest;
import org.mapnaom.asset.dto.ReferenceDtos.NamedRequest;
import org.mapnaom.asset.dto.ReferenceDtos.PersonRequest;
import org.mapnaom.asset.entity.CostCenter;
import org.mapnaom.asset.entity.Location;
import org.mapnaom.asset.entity.Person;
import org.mapnaom.asset.entity.Project;
import org.mapnaom.asset.repository.CostCenterRepository;
import org.mapnaom.asset.repository.LocationRepository;
import org.mapnaom.asset.repository.PersonRepository;
import org.mapnaom.asset.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ReferenceExcelService {

    private static final List<String> COST_CENTER_HEADERS = List.of("code", "name", "active");
    private static final List<String> PROJECT_HEADERS = List.of("code", "name", "active");
    private static final List<String> LOCATION_HEADERS = List.of("code", "name", "address", "active");
    private static final List<String> PERSON_HEADERS = List.of("personnelCode", "fullName", "active");

    private final CostCenterRepository costCenterRepository;
    private final ProjectRepository projectRepository;
    private final LocationRepository locationRepository;
    private final PersonRepository personRepository;

    @Transactional
    public ImportResult importCostCenters(MultipartFile file) {
        return importNamedRows(file, COST_CENTER_HEADERS, "Cost center", row -> {
            NamedRequest request = new NamedRequest(text(row, 0), text(row, 1), parseBoolean(text(row, 2), "active"));
            return upsertCostCenter(request);
        });
    }

    @Transactional
    public ImportResult importProjects(MultipartFile file) {
        return importNamedRows(file, PROJECT_HEADERS, "Project", row -> {
            NamedRequest request = new NamedRequest(text(row, 0), text(row, 1), parseBoolean(text(row, 2), "active"));
            return upsertProject(request);
        });
    }

    @Transactional
    public ImportResult importLocations(MultipartFile file) {
        return importRows(file, LOCATION_HEADERS, "Location", row -> {
            LocationRequest request = new LocationRequest(text(row, 0), text(row, 1), text(row, 2), parseBoolean(text(row, 3), "active"));
            return upsertLocation(request);
        });
    }

    @Transactional
    public ImportResult importPeople(MultipartFile file) {
        return importRows(file, PERSON_HEADERS, "Person", row -> {
            PersonRequest request = new PersonRequest(text(row, 0), text(row, 1), parseBoolean(text(row, 2), "active"));
            return upsertPerson(request);
        });
    }

    private ImportResult importNamedRows(MultipartFile file, List<String> headers, String label, RowConsumer consumer) {
        return importRows(file, headers, label, consumer);
    }

    private ImportResult importRows(MultipartFile file, List<String> headers, String label, RowConsumer consumer) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Excel file is required");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase(Locale.ROOT).endsWith(".xlsx")) {
            throw new IllegalArgumentException("Only .xlsx files are supported");
        }

        int created = 0;
        int updated = 0;
        Set<String> seen = new HashSet<>();
        DataFormatter formatter = new DataFormatter();
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getNumberOfSheets() == 0 ? null : workbook.getSheetAt(0);
            if (sheet == null || sheet.getPhysicalNumberOfRows() == 0) {
                throw new IllegalArgumentException("The first worksheet is empty");
            }
            validateHeaders(sheet.getRow(sheet.getFirstRowNum()), headers, formatter);

            for (int rowIndex = sheet.getFirstRowNum() + 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null || isEmpty(row, formatter, headers.size())) {
                    continue;
                }
                String code = text(row, 0).trim();
                if (code.isEmpty()) {
                    throw new IllegalArgumentException(label + " code is required at row " + (rowIndex + 1));
                }
                if (!seen.add(code)) {
                    continue;
                }
                try {
                    boolean createdRow = consumer.accept(row);
                    if (createdRow) {
                        created++;
                    } else {
                        updated++;
                    }
                } catch (RuntimeException exception) {
                    throw new IllegalArgumentException("Excel row " + (rowIndex + 1) + ": " + exception.getMessage(), exception);
                }
            }
        } catch (IOException exception) {
            throw new IllegalArgumentException("Cannot read Excel file", exception);
        }

        return new ImportResult(created, updated, created + updated);
    }

    private boolean upsertCostCenter(NamedRequest request) {
        CostCenter entity = costCenterRepository.findByCode(request.code().trim())
                .orElseGet(CostCenter::new);
        boolean isNew = entity.getId() == null;
        entity.setCode(request.code().trim());
        entity.setName(request.name().trim());
        entity.setActive(request.active());
        costCenterRepository.save(entity);
        return isNew;
    }

    private boolean upsertProject(NamedRequest request) {
        Project entity = projectRepository.findByCode(request.code().trim())
                .orElseGet(Project::new);
        boolean isNew = entity.getId() == null;
        entity.setCode(request.code().trim());
        entity.setName(request.name().trim());
        entity.setActive(request.active());
        projectRepository.save(entity);
        return isNew;
    }

    private boolean upsertLocation(LocationRequest request) {
        Location entity = locationRepository.findByCode(request.code().trim())
                .orElseGet(Location::new);
        boolean isNew = entity.getId() == null;
        entity.setCode(request.code().trim());
        entity.setName(request.name().trim());
        entity.setAddress(trimToNull(request.address()));
        entity.setActive(request.active());
        locationRepository.save(entity);
        return isNew;
    }

    private boolean upsertPerson(PersonRequest request) {
        Person entity = personRepository.findByPersonnelCode(request.personnelCode().trim())
                .orElseGet(Person::new);
        boolean isNew = entity.getId() == null;
        entity.setPersonnelCode(request.personnelCode().trim());
        entity.setFullName(request.fullName().trim());
        entity.setActive(request.active());
        personRepository.save(entity);
        return isNew;
    }

    private void validateHeaders(Row row, List<String> expectedHeaders, DataFormatter formatter) {
        if (row == null) {
            throw new IllegalArgumentException("Header row is missing");
        }
        for (int index = 0; index < expectedHeaders.size(); index++) {
            String actual = text(row, index, formatter);
            if (!expectedHeaders.get(index).equals(actual)) {
                throw new IllegalArgumentException("Expected column " + (index + 1) + " to be '" + expectedHeaders.get(index) + "'");
            }
        }
    }

    private boolean isEmpty(Row row, DataFormatter formatter, int headerCount) {
        for (int index = 0; index < headerCount; index++) {
            if (!text(row, index, formatter).isBlank()) {
                return false;
            }
        }
        return true;
    }

    private String text(Row row, int index) {
        return text(row, index, new DataFormatter());
    }

    private String text(Row row, int index, DataFormatter formatter) {
        Cell cell = row.getCell(index, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        return cell == null ? "" : formatter.formatCellValue(cell).trim();
    }

    private boolean parseBoolean(String value, String field) {
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "true", "1", "yes", "y", "active" -> true;
            case "false", "0", "no", "n", "inactive" -> false;
            default -> throw new IllegalArgumentException(field + " must be true/false");
        };
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    @FunctionalInterface
    private interface RowConsumer {
        boolean accept(Row row);
    }
}
