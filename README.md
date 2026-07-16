# Melos Market

Marketplace base for connecting customers with trade workers such as plumbers, builders, electricians, and other service providers.

## Structure

- `backend/` - Java Spring Boot API
- `frontend/` - Frontend app placeholder
- `docker-compose.yml` - Local PostgreSQL database

## Local Database

Start PostgreSQL:

```bash
docker compose up -d postgres
```

The backend is configured to connect to:

- Host: `localhost`
- Port: `5432`
- Database: `melosmarket`
- User: `melosmarket`
- Password: `melosmarket`

## Backend

Run the backend from `backend/`:

```bash
mvn spring-boot:run
```

Health endpoint:

```bash
curl http://localhost:8080/api/health
```

