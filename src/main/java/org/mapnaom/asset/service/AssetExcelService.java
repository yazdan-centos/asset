package org.mapnaom.asset.service;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.mapnaom.asset.dto.AssetRequest;
import org.mapnaom.asset.dto.AssetResponse;
import org.mapnaom.asset.dto.ImportResult;
import org.mapnaom.asset.entity.enums.AssetStatus;
import org.mapnaom.asset.entity.enums.DepreciationStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AssetExcelService {

    private static final List<String> HEADERS = List.of(
            "plateNumber", "title", "commissioningDate", "assetGroup", "depreciationMethod",
            "costCenterCode", "projectCode", "locationCode", "custodianPersonnelCode",
            "responsiblePersonnelCode", "acquisitionCost", "accumulatedDepreciation",
            "status", "depreciationStatus"
    );

    private final AssetService assetService;
    private final Validator validator;

    @Transactional
    public ImportResult importFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Excel file is required");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".xlsx")) {
            throw new IllegalArgumentException("Only .xlsx files are supported");
        }

        int created = 0;
        int updated = 0;
        DataFormatter formatter = new DataFormatter();
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getNumberOfSheets() == 0 ? null : workbook.getSheetAt(0);
            if (sheet == null || sheet.getPhysicalNumberOfRows() == 0) {
                throw new IllegalArgumentException("The first worksheet is empty");
            }
            validateHeaders(sheet.getRow(sheet.getFirstRowNum()), formatter);

            for (int rowIndex = sheet.getFirstRowNum() + 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null || isEmpty(row, formatter)) {
                    continue;
                }
                try {
                    AssetRequest request = toRequest(row, formatter);
                    validate(request);
                    if (assetService.upsert(request)) {
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

    public byte[] exportFile() {
        List<AssetResponse> assets = assetService.findAll();
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Assets");
            CellStyle headerStyle = createHeaderStyle(workbook);
            Row header = sheet.createRow(0);
            for (int index = 0; index < HEADERS.size(); index++) {
                Cell cell = header.createCell(index);
                cell.setCellValue(HEADERS.get(index));
                cell.setCellStyle(headerStyle);
            }

            for (int index = 0; index < assets.size(); index++) {
                writeAsset(sheet.createRow(index + 1), assets.get(index));
            }
            for (int index = 0; index < HEADERS.size(); index++) {
                sheet.autoSizeColumn(index);
            }
            sheet.createFreezePane(0, 1);
            workbook.write(output);
            return output.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot create Excel file", exception);
        }
    }

    private void validateHeaders(Row row, DataFormatter formatter) {
        if (row == null) {
            throw new IllegalArgumentException("Header row is missing");
        }
        for (int index = 0; index < HEADERS.size(); index++) {
            String actual = text(row, index, formatter);
            if (!HEADERS.get(index).equals(actual)) {
                throw new IllegalArgumentException("Expected column " + (index + 1) + " to be '" + HEADERS.get(index) + "'");
            }
        }
    }

    private AssetRequest toRequest(Row row, DataFormatter formatter) {
        return new AssetRequest(
                text(row, 0, formatter),
                text(row, 1, formatter),
                parseDate(text(row, 2, formatter), "commissioningDate"),
                text(row, 3, formatter),
                text(row, 4, formatter),
                text(row, 5, formatter),
                nullIfBlank(text(row, 6, formatter)),
                text(row, 7, formatter),
                text(row, 8, formatter),
                text(row, 9, formatter),
                parseDecimal(text(row, 10, formatter), "acquisitionCost"),
                parseDecimal(text(row, 11, formatter), "accumulatedDepreciation"),
                parseEnum(AssetStatus.class, text(row, 12, formatter), "status"),
                parseEnum(DepreciationStatus.class, text(row, 13, formatter), "depreciationStatus")
        );
    }

    private void validate(AssetRequest request) {
        Set<ConstraintViolation<AssetRequest>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            ConstraintViolation<AssetRequest> violation = violations.iterator().next();
            throw new IllegalArgumentException(violation.getPropertyPath() + " " + violation.getMessage());
        }
    }

    private void writeAsset(Row row, AssetResponse asset) {
        set(row, 0, asset.plateNumber());
        set(row, 1, asset.title());
        set(row, 2, asset.commissioningDate().toString());
        set(row, 3, asset.assetGroup());
        set(row, 4, asset.depreciationMethod());
        set(row, 5, asset.costCenter().code());
        set(row, 6, asset.project() == null ? "" : asset.project().code());
        set(row, 7, asset.location().code());
        set(row, 8, asset.custodian().personnelCode());
        set(row, 9, asset.responsiblePerson().personnelCode());
        set(row, 10, asset.acquisitionCost().toPlainString());
        set(row, 11, asset.accumulatedDepreciation().toPlainString());
        set(row, 12, asset.status().name());
        set(row, 13, asset.depreciationStatus().name());
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        Font font = workbook.createFont();
        font.setBold(true);
        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        return style;
    }

    private boolean isEmpty(Row row, DataFormatter formatter) {
        for (int index = 0; index < HEADERS.size(); index++) {
            if (!text(row, index, formatter).isBlank()) {
                return false;
            }
        }
        return true;
    }

    private String text(Row row, int index, DataFormatter formatter) {
        Cell cell = row.getCell(index, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        return cell == null ? "" : formatter.formatCellValue(cell).trim();
    }

    private LocalDate parseDate(String value, String field) {
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException(field + " must use yyyy-MM-dd");
        }
    }

    private BigDecimal parseDecimal(String value, String field) {
        try {
            return new BigDecimal(value.replace(",", ""));
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(field + " must be a number");
        }
    }

    private <E extends Enum<E>> E parseEnum(Class<E> type, String value, String field) {
        try {
            return Enum.valueOf(type, value.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(field + " has an unsupported value: " + value);
        }
    }

    private String nullIfBlank(String value) {
        return value.isBlank() ? null : value;
    }

    private void set(Row row, int index, String value) {
        row.createCell(index).setCellValue(value);
    }
}
