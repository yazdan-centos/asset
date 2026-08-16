package org.mapnaom.asset.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.mapnaom.asset.dto.AssetRequest;
import org.mapnaom.asset.dto.AssetResponse;
import org.mapnaom.asset.dto.ImportResult;
import org.mapnaom.asset.service.AssetExcelService;
import org.mapnaom.asset.service.AssetService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
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
@RequestMapping("/api/assets")
@RequiredArgsConstructor
public class AssetController {

    private static final MediaType XLSX = MediaType.parseMediaType(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    private final AssetService assetService;
    private final AssetExcelService excelService;

    @GetMapping
    public List<AssetResponse> findAll() {
        return assetService.findAll();
    }

    @GetMapping("/{id}")
    public AssetResponse findById(@PathVariable Long id) {
        return assetService.findById(id);
    }

    @PostMapping
    public ResponseEntity<AssetResponse> create(@Valid @RequestBody AssetRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(assetService.create(request));
    }

    @PutMapping("/{id}")
    public AssetResponse update(@PathVariable Long id, @Valid @RequestBody AssetRequest request) {
        return assetService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        assetService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ImportResult importExcel(@RequestPart("file") MultipartFile file) {
        return excelService.importFile(file);
    }

    @GetMapping(value = "/export", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public ResponseEntity<byte[]> exportExcel() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(XLSX);
        headers.setContentDisposition(ContentDisposition.attachment().filename("assets.xlsx").build());
        return new ResponseEntity<>(excelService.exportFile(), headers, HttpStatus.OK);
    }
}
