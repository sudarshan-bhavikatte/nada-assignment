# Nada Network — People & Connections API

## Endpoints

| Method | Path | Description |
|--------|------|-------------|
| POST   | `/people` | `{"name":"Asha"}` → `{"id":1,"name":"Asha"}` |
| POST   | `/knows` | `{"a":1,"b":2}` → `204`. Duplicate is idempotent. |
| GET    | `/people/{id}/contacts` | `[{"id":2,"name":"Rohan"}]` — empty list if none |
| GET    | `/path?from=1&to=5` | `{"connected":true,"hops":2,"path":[...]}` or `{"connected":false}` |

### Errors

- `404 {"error":"no person 9"}` — unknown person ID
- `400` — invalid JSON, missing required field, blank name, `a == b` in `/knows`, or missing/non-numeric `from`/`to` in `/path`

Names in any script (including Hindi) are stored and returned exactly as supplied.

## How to run

Requires Java 17+.

    ./mvnw spring-boot:run        

The server starts on port **8080**.

## How to run tests

    ./mvnw test

## Next steps with more time

- Persistence with database.
- Pagination for contacts, response-size caps
- OpenAPI docs, request logging
- Dockerfile + GitHub Actions CI

## AI tools used

- Kimi (Moonshot AI) — project scaffolding, tests, README