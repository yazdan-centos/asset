package org.mapnaom.asset;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.mapnaom.asset.dto.ImportResult;
import org.mapnaom.asset.dto.ReferenceDtos.NamedResponse;
import org.mapnaom.asset.service.ReferenceDataService;
import org.mapnaom.asset.service.ReferenceExcelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ReferenceExcelImportIntegrationTests {

    @Autowired
    private ReferenceDataService referenceDataService;

    @Autowired
    private ReferenceExcelService referenceExcelService;

    @Test
    void costCenterImportSkipsRepeatedRowsInTheSameFile() throws IOException {
        byte[] workbook = createWorkbook(
                new String[]{"code", "name", "active"},
                new String[]{"CC-01", "Operations", "true"},
                new String[]{"CC-01", "Operations", "true"},
                new String[]{"CC-02", "Finance", "false"}
        );

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "cost-centers.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                workbook
        );

        ImportResult result = referenceExcelService.importCostCenters(file);

        assertThat(result).isEqualTo(new ImportResult(2, 0, 2));
        assertThat(referenceDataService.findAllCostCenters())
                .extracting(NamedResponse::code)
                .containsExactlyInAnyOrder("CC-01", "CC-02");
    }

    private byte[] createWorkbook(String[] headers, String[]... rows) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("CostCenters");
            Row headerRow = sheet.createRow(0);
            for (int index = 0; index < headers.length; index++) {
                headerRow.createCell(index).setCellValue(headers[index]);
            }
            for (int rowIndex = 0; rowIndex < rows.length; rowIndex++) {
                Row row = sheet.createRow(rowIndex + 1);
                String[] values = rows[rowIndex];
                for (int cellIndex = 0; cellIndex < values.length; cellIndex++) {
                    row.createCell(cellIndex).setCellValue(values[cellIndex]);
                }
            }
            workbook.write(output);
            return output.toByteArray();
        }
    }
}
