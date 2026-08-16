# Asset API

Spring Boot REST API for asset management with CRUD operations and Excel import/export.

## Authentication and authorization

- Login: `POST /api/auth/login` with `username` and `password`.
- The response contains one JWT field named `accessToken`.
- Send the token as `Authorization: Bearer <accessToken>`.
- Authentication is stateless. No HTTP session, sign-out endpoint, or refresh token is used.
- Users and roles are loaded from PostgreSQL. Passwords are stored as BCrypt hashes.
- `ROLE_USER` and `ROLE_ADMIN` may read asset/reference endpoints.
- Only `ROLE_ADMIN` may create, update, delete, import Excel files, or access `/api/users` and `/api/roles`.

Set `APP_JWT_SECRET` to a secret of at least 32 bytes before startup. To create the first administrator in an empty database, set `APP_INITIAL_ADMIN_USERNAME` and `APP_INITIAL_ADMIN_PASSWORD`; the password must contain at least eight characters. The bootstrap credentials are only used when that username does not already exist.

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
