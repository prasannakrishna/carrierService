# Domain Worker Management Pattern

## Overview

Each domain service (carrier, seller, warehouse, store) has its own local worker management system for mobile app users (operators). This is separate from the platform users (userService/Keycloak) who access web dashboards.

## Two-Tier User Model

| Tier | Who | Access | Auth | Managed By |
|------|-----|--------|------|-----------|
| Platform Users | Admin, Associates, Managers | Web apps | Keycloak JWT + DPoP | userService |
| Domain Workers | Drivers, Pickers, Packers, Store Associates | Mobile apps | Phone + PIN (local) | Each domain service |

## Entities (same pattern in each service)

### DomainRole (table: `domain_roles`)
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| roleId | String | Auto (ROLE-XXXXXXXX) | PK |
| roleName | String | Yes | Free text: "Driver", "Picker", etc. |
| roleType | String | Yes | `OPERATOR` or `MANAGER` |
| description | String | No | What this role does |
| permissions | TEXT/JSON | No | App features this role can access |
| isActive | Boolean | Yes (default true) | Soft delete |

### DomainWorker (table: `domain_workers`)
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| workerId | String | Auto (WRK-XXXXXXXX) | PK |
| name | String | Yes | Full name |
| phone | String | Yes | Mobile number (used for login) |
| email | String | No | Optional email |
| roleId | String | Yes | FK to DomainRole |
| pin | String | No | 4-6 digit PIN for mobile login |
| platformUserId | String | No | Links to userService if also a web user |
| driverId | String | No | (carrier only) links to Driver entity |
| status | String | Yes | ACTIVE, INACTIVE, ON_LEAVE |
| deviceToken | String | No | Push notification token |
| lastLoginAt | DateTime | No | Auto-updated on login |

## API Endpoints (same pattern in each service)

Replace `/carrier/` with the domain prefix:

### Roles
```
POST   /api/v1/{domain}/domain-workers/roles         — Create role
GET    /api/v1/{domain}/domain-workers/roles          — List roles (?roleType=OPERATOR)
PUT    /api/v1/{domain}/domain-workers/roles/{id}     — Update role
DELETE /api/v1/{domain}/domain-workers/roles/{id}     — Deactivate role
```

### Workers
```
POST   /api/v1/{domain}/domain-workers               — Create worker
GET    /api/v1/{domain}/domain-workers               — List workers (?roleId=X&status=ACTIVE)
GET    /api/v1/{domain}/domain-workers/{id}          — Get worker
PUT    /api/v1/{domain}/domain-workers/{id}          — Update worker
DELETE /api/v1/{domain}/domain-workers/{id}          — Deactivate worker
PATCH  /api/v1/{domain}/domain-workers/{id}/pin      — Set/reset PIN
```

### Mobile Auth
```
POST   /api/v1/{domain}/domain-workers/auth/login    — Phone + PIN login
```

## Per-Domain Role Examples

### carrierService (port 8084)
| Role Name | Type | Mobile App Access |
|-----------|------|-------------------|
| Driver | OPERATOR | Accept trips, post milestones, POD upload |
| Helper | OPERATOR | Assist loading/unloading, scan packages |
| Dispatcher | MANAGER | Assign trips, monitor fleet |
| Fleet Supervisor | MANAGER | Approve routes, manage exceptions |

### sellerService (port 8124)
| Role Name | Type | Mobile App Access |
|-----------|------|-------------------|
| Production Worker | OPERATOR | Execute work orders, log output |
| QC Inspector | OPERATOR | Quality check, grade products |
| Packaging Operator | OPERATOR | Pack items, print labels |
| Production Supervisor | MANAGER | Approve batches, manage workers |

### warehouseService (WMS)
| Role Name | Type | Mobile App Access |
|-----------|------|-------------------|
| Picker | OPERATOR | Pick items from locations |
| Packer | OPERATOR | Pack orders for shipping |
| Put-away Operator | OPERATOR | Receive + shelve stock |
| Forklift Operator | OPERATOR | Move pallets between zones |
| Warehouse Supervisor | MANAGER | Assign tasks, resolve exceptions |

### storeService
| Role Name | Type | Mobile App Access |
|-----------|------|-------------------|
| Store Associate | OPERATOR | Fulfill orders, stock shelves |
| Cashier | OPERATOR | Process payments, returns |
| Delivery Boy | OPERATOR | Last-mile delivery, collect POD |
| Store Manager | MANAGER | Manage inventory, approve returns |

## Mobile App Login Flow

```
1. Admin creates DomainRole (e.g. "Driver") via web dashboard
2. Admin creates DomainWorker (name, phone, roleId) via web dashboard
3. Admin sets PIN for worker (PATCH /domain-workers/{id}/pin)
4. Worker downloads mobile app
5. Worker enters phone + PIN
6. App calls POST /api/v1/carrier/domain-workers/auth/login
7. Service validates → returns worker info + role + permissions
8. App shows screens based on permissions JSON
```

## Integration with Platform Users

A `DomainWorker` with `roleType=MANAGER` can optionally link to a platform user:
- `platformUserId` → references the user in userService
- This means the same person has both web access (via Keycloak) and mobile access (via PIN)
- The Fleet `manager` field references the platform user's username

## Implementation Status

| Service | DomainRole | DomainWorker | Controller | Mobile Auth |
|---------|-----------|-------------|-----------|-------------|
| carrierService | ✅ | ✅ | ✅ | ✅ |
| sellerService | ✅ (entity created) | Existing Worker entity (needs PIN field) | TODO | TODO |
| warehouseService | TODO | TODO | TODO | TODO |
| storeService | TODO | TODO | TODO | TODO |
