# Frontend Developer Guide (React + JSX)

This project exposes a Spring Boot REST API for asset management. The React frontend should treat the backend as the source of truth and keep authentication, permission checks, and form validation in the client layer.

## 1. Project context

- API base: configured by the backend environment or reverse proxy
- Auth: stateless JWT authentication
- Roles: `ROLE_USER` and `ROLE_ADMIN`
- Main API resources:
  - `/api/assets`
  - `/api/cost-centers`
  - `/api/projects`
  - `/api/locations`
  - `/api/people`
  - `/api/users`
  - `/api/roles`

## 2. React app setup

Use a Vite-based React app or equivalent React environment.

### Environment variables

```env
VITE_API_BASE_URL=http://localhost:8080
```

Recommended structure:

```text
src/
  api/
    authApi.js
    assetsApi.js
    referencesApi.js
    client.js
  components/
  hooks/
    useAuth.js
  pages/
    LoginPage.jsx
    AssetsPage.jsx
    AssetForm.jsx
  routes/
    ProtectedRoute.jsx
    AdminRoute.jsx
  context/
    AuthContext.jsx
```

## 3. Authentication flow

### Login request

- Endpoint: `POST /api/auth/login`
- Body:

```json
{
  "username": "admin",
  "password": "secret"
}
```

- Response:

```json
{
  "accessToken": "<jwt>"
}
```

### Token handling

Use a shared API client to attach the token to every authenticated request.

```jsx
// src/api/client.js
import axios from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken');
  if (token) {
    config.headers.Authorization = token;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('accessToken');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default api;
```

### Login component pattern

```jsx
const handleLogin = async (values) => {
  const { data } = await api.post('/api/auth/login', values);
  localStorage.setItem('accessToken', data.accessToken);
  navigate('/assets');
};
```

## 4. Role-based access control

- `ROLE_USER`: read asset/reference data
- `ROLE_ADMIN`: create, update, delete, import Excel, and access `/api/users` and `/api/roles`

### React route guards

```jsx
// ProtectedRoute.jsx
import { Navigate } from 'react-router-dom';

export default function ProtectedRoute({ children }) {
  const token = localStorage.getItem('accessToken');
  return token ? children : <Navigate to="/login" replace />;
}
```

```jsx
// AdminRoute.jsx
import { Navigate } from 'react-router-dom';

export default function AdminRoute({ children, user }) {
  if (!user) return <Navigate to="/login" replace />;
  return user.roles.includes('ROLE_ADMIN') ? children : <Navigate to="/assets" replace />;
}
```

Frontend should hide or disable admin-only actions for non-admin users.

## 5. API conventions in React

### CRUD pattern

Each main resource is usually wrapped in a service file:

```jsx
// src/api/assetsApi.js
import api from './client';

export const getAssets = () => api.get('/api/assets');
export const getAssetById = (id) => api.get(`/api/assets/${id}`);
export const createAsset = (payload) => api.post('/api/assets', payload);
export const updateAsset = (id, payload) => api.put(`/api/assets/${id}`, payload);
export const deleteAsset = (id) => api.delete(`/api/assets/${id}`);
```

### Excel import/export

- Export all assets: `GET /api/assets/export`
- Import assets: `POST /api/assets/import` with `multipart/form-data`
- Import cost centers: `POST /api/cost-centers/import`
- Import projects: `POST /api/projects/import`
- Import locations: `POST /api/locations/import`
- Import people: `POST /api/people/import`
- Only `.xlsx` files are accepted
- Import is atomic and rolls back on any failure
- Duplicate rows inside the same file are ignored after the first occurrence by key field (`plateNumber` for assets, `code` for cost centers/projects/locations, `personnelCode` for people)

Example upload:

```jsx
const formData = new FormData();
formData.append('file', selectedFile);

await api.post('/api/assets/import', formData, {
  headers: { 'Content-Type': 'multipart/form-data' },
});
```

## 6. Recommended React patterns

- Use `useState` and `useEffect` for simple list/detail flows.
- Use `useMemo` for derived values and filtered tables.
- Keep API calls in a service layer instead of embedding fetch logic inside components.
- Use `React Router` for protected/admin routes.
- Use `react-hook-form` or `Formik` for richer forms.
- Prefer a single source of truth for auth state via `AuthContext`.

### Example auth context

```jsx
const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);

  const login = async (credentials) => {
    const { data } = await api.post('/api/auth/login', credentials);
    localStorage.setItem('accessToken', data.accessToken);
    setUser({ roles: ['ROLE_ADMIN'] });
  };

  const logout = () => {
    localStorage.removeItem('accessToken');
    setUser(null);
  };

  return <AuthContext.Provider value={{ user, login, logout }}>{children}</AuthContext.Provider>;
}
```

## 7. UI guidance

### Core screens

- Login page
- Asset list page
- Asset detail page
- Asset create/edit form
- Cost center / project / location / person management pages
- Reference Excel import pages for admin users
- Admin user and role management pages
- Excel import/export controls for admin users

### UX expectations

- Display validation messages inline with the form fields.
- Confirm delete actions before calling the backend.
- Show spinner/loading state while fetching.
- Display empty state and error state for tables and details.
- Keep list views filterable and sortable when data grows.

## 8. Form handling and data mapping

The backend expects dates in `yyyy-MM-dd` format and enum values using Java names. Common enum values include:

- Asset status: `PLATED`, `PENDING_TRANSFER`, `OUT_OF_ORGANIZATION`, `SOLD`, `SCRAPPED`, `DELETED`, `PENDING_EXIT_FROM_ORGANIZATION`, `ASSET_SET_ASIDE`, `TEMPORARY_EXIT`, `TRANSFERRED_TO_WAREHOUSE`, `REPLATED`
- Depreciation status: `NOT_DEPRECIATED`, `NON_DEPRECIABLE`, `DEPRECIATED`
- Asset group: `TECHNICAL_TOOL_STRAIGHT_10_YEARS`, `OFFICE_FURNITURE_STRAIGHT_15_YEARS`, `OFFICE_FURNITURE_STRAIGHT_10_YEARS`, `OFFICE_FURNITURE_STRAIGHT_3_YEARS`, `OFFICE_FURNITURE_STRAIGHT_5_YEARS`, `LAND`, `BUILDING_STRAIGHT_15_YEARS`, `BUILDING_STRAIGHT_25_YEARS`, `SOFTWARE_STRAIGHT_3_YEARS`, `VEHICLE_STRAIGHT_4_YEARS`, `VEHICLE_STRAIGHT_6_YEARS`
- Depreciation method: `STRAIGHT_LINE_10_YEARS`, `STRAIGHT_LINE_15_YEARS`, `STRAIGHT_LINE_25_YEARS`, `STRAIGHT_LINE_3_YEARS`, `STRAIGHT_LINE_4_YEARS`, `STRAIGHT_LINE_5_YEARS`, `STRAIGHT_LINE_6_YEARS`

When building JSX forms, map the labels to the exact backend fields:

```jsx
const assetPayload = {
  plateNumber: values.plateNumber,
  title: values.title,
  commissioningDate: formatDate(values.commissioningDate),
  assetGroup: values.assetGroup, // e.g. 'OFFICE_FURNITURE_STRAIGHT_5_YEARS'
  depreciationMethod: values.depreciationMethod, // e.g. 'STRAIGHT_LINE_5_YEARS'
  status: values.status,
  depreciationStatus: values.depreciationStatus,
};
```

Use the enum names from the backend, not a translated Persian label, when sending payloads. For reference data imports and management screens, keep the UI value as the exact backend code as well.

## 9. Error handling in React

Typical client handling rules:

- 400: show validation or request error message
- 401: clear token and redirect to login
- 403: show access denied and hide privileged actions
- 404: show not found state
- 500: show generic server error message

Example:

```jsx
try {
  await api.post('/api/assets', payload);
  toast.success('Asset saved successfully');
} catch (error) {
  if (error.response?.status === 403) {
    toast.error('You do not have permission to perform this action.');
    return;
  }

  toast.error('Something went wrong while saving the asset.');
}
```

## 10. Development workflow

Run the backend locally before testing the frontend:

```bash
./mvnw spring-boot:run
```

Required backend environment variables include:

- `APP_JWT_SECRET` (minimum 32 bytes)
- PostgreSQL datasource settings
- Optional bootstrap admin credentials:
  - `APP_INITIAL_ADMIN_USERNAME`
  - `APP_INITIAL_ADMIN_PASSWORD`

## 11. Testing recommendations

For a React codebase, validate:

- login/logout behavior
- route protection for logged-in and admin-only pages
- form validation for required asset fields
- API errors and loading states
- Excel upload logic for admin users
- role-based rendering of admin-only UI

Suggested tools:

- `Vitest` or `Jest`
- `React Testing Library`
- `MSW` for mocking API responses

## 12. Quick checklist

- [ ] Set `VITE_API_BASE_URL`
- [ ] Add JWT interceptor in shared API client
- [ ] Protect private and admin-only routes
- [ ] Clear token on `401` and redirect to login
- [ ] Validate asset and reference forms before submit
- [ ] Show loading, empty, and error states
- [ ] Support Excel import/export actions for admins

This document should be treated as the frontend contract for consuming the asset API in a React + JSX application while the backend continues to evolve.
