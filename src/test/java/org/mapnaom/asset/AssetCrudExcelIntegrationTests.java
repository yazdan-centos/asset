package org.mapnaom.asset;

import org.junit.jupiter.api.Test;
import org.mapnaom.asset.dto.AssetRequest;
import org.mapnaom.asset.dto.AssetResponse;
import org.mapnaom.asset.dto.ImportResult;
import org.mapnaom.asset.dto.ReferenceDtos.LocationRequest;
import org.mapnaom.asset.dto.ReferenceDtos.NamedRequest;
import org.mapnaom.asset.dto.ReferenceDtos.PersonRequest;
import org.mapnaom.asset.entity.enums.AssetGroup;
import org.mapnaom.asset.entity.enums.AssetStatus;
import org.mapnaom.asset.entity.enums.DepreciationMethod;
import org.mapnaom.asset.entity.enums.DepreciationStatus;
import org.mapnaom.asset.service.AssetExcelService;
import org.mapnaom.asset.service.AssetService;
import org.mapnaom.asset.service.ReferenceDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class AssetCrudExcelIntegrationTests {

    @Autowired
    private ReferenceDataService referenceDataService;

    @Autowired
    private AssetService assetService;

    @Autowired
    private AssetExcelService excelService;

    @Test
    void crudAndExcelRoundTripPreserveAssetData() {
        referenceDataService.createCostCenter(new NamedRequest("CC-01", "Operations", true));
        referenceDataService.createProject(new NamedRequest("PR-01", "Expansion", true));
        referenceDataService.createLocation(new LocationRequest("LOC-01", "Head Office", "Building A", true));
        referenceDataService.createPerson(new PersonRequest("P-100", "Asset Custodian", true));
        referenceDataService.createPerson(new PersonRequest("P-200", "Responsible Manager", true));

        AssetRequest original = request("Laptop", new BigDecimal("1250.00"));
        AssetResponse created = assetService.create(original);
        assertThat(assetService.findById(created.id()).bookValue()).isEqualByComparingTo("1000.00");

        AssetResponse updated = assetService.update(created.id(), request("Engineering Laptop", new BigDecimal("1500.00")));
        assertThat(updated.title()).isEqualTo("Engineering Laptop");

        byte[] workbook = excelService.exportFile();
        assertThat(workbook).isNotEmpty();

        assetService.delete(created.id());
        assertThat(assetService.findAll()).isEmpty();

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "assets.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                workbook
        );
        ImportResult result = excelService.importFile(file);

        assertThat(result).isEqualTo(new ImportResult(1, 0, 1));
        assertThat(assetService.findAll())
                .singleElement()
                .satisfies(asset -> {
                    assertThat(asset.plateNumber()).isEqualTo("AST-001");
                    assertThat(asset.title()).isEqualTo("Engineering Laptop");
                    assertThat(asset.project().code()).isEqualTo("PR-01");
                    assertThat(asset.acquisitionCost()).isEqualByComparingTo("1500.00");
                });
    }

    private AssetRequest request(String title, BigDecimal acquisitionCost) {
        return new AssetRequest(
                "AST-001",
                title,
                LocalDate.of(2025, 1, 15),
                AssetGroup.OFFICE_FURNITURE_STRAIGHT_5_YEARS,
                DepreciationMethod.STRAIGHT_LINE_5_YEARS,
                "CC-01",
                "PR-01",
                "LOC-01",
                "P-100",
                "P-200",
                acquisitionCost,
                new BigDecimal("250.00"),
                AssetStatus.PLATED,
                DepreciationStatus.NOT_DEPRECIATED
        );
    }
}
