# Asset API

Spring Boot REST API for asset management with CRUD operations and Excel import/export.

## CRUD endpoints

| Resource | Collection endpoint |
| --- | --- |
| Assets | `/api/assets` |
| Cost centers | `/api/cost-centers` |
| Projects | `/api/projects` |
| Locations | `/api/locations` |
| People | `/api/people` |

Each resource supports `GET` collection, `GET /{id}`, `POST`, `PUT /{id}`, and `DELETE /{id}`.
Reference records must be created before creating or importing assets.

## Excel import/export

- Export all assets: `GET /api/assets/export`
- Import or update assets: `POST /api/assets/import` as `multipart/form-data` with a `file` part
- Only `.xlsx` files are accepted.
- Import is atomic: a failure in any row rolls back the complete file.
- Existing assets are updated by `plateNumber`; unknown plate numbers are created.

The first worksheet must use this exact header row and order:

```text
plateNumber,title,commissioningDate,assetGroup,depreciationMethod,costCenterCode,projectCode,locationCode,custodianPersonnelCode,responsiblePersonnelCode,acquisitionCost,accumulatedDepreciation,status,depreciationStatus
```

Dates use `yyyy-MM-dd`. Enum values use the Java names from `AssetStatus` and `DepreciationStatus`, such as `ACTIVE` and `IN_PROGRESS`. `projectCode` may be blank; all other relationship codes must already exist.

## Run

Configure the PostgreSQL datasource with standard Spring Boot datasource properties, then run:

```bash
./mvnw spring-boot:run
```

Run tests with:

```bash
./mvnw test
```
