# Software Design Project – Railway Deployment

This repository contains two Spring Boot applications:

- `Project_demo`: the web front-end that calls the staff API.
- `Project_demo_API`: the REST API that serves staff data.

The repository now includes container images and a `railway.toml` configuration so that each
application can be deployed to [Railway](https://railway.app/) as an independent service.

## Deployment overview

Railway can deploy each service either from the provided `Dockerfile` or by using the
`railway.toml` file to run Maven builds with Nixpacks. The typical setup is:

1. Create a Railway project and link this repository.
2. Railway will detect `railway.toml` and create two services:
   - **project_demo_web** – builds the `Project_demo` module and runs the web application.
   - **project_demo_api** – builds the `Project_demo_API` module and runs the REST API.
3. The `start.sh` helper script automatically runs the correct module based on the service
   name (or the `SERVICE_MODULE` environment variable) and will build the application if a
   packaged JAR is not already available. Hyphenated Railway service names such as
   `project-demo-api` or `myproject-web` are detected automatically, so manual overrides are
   rarely needed.
4. Configure environment variables for each service as described below.

## Environment variables

### Web application (`Project_demo`)

| Variable | Default | Description |
|----------|---------|-------------|
| `PORT` | `8082` | Port exposed by Railway (set automatically to `8080` when deployed). |
| `STAFF_API_BASE_URL` | `http://localhost:8085/api/staff` | Public URL of the API service. |
| `STAFF_API_TIMEOUT` | `5s` | Client timeout when calling the API. |
| `SERVICE_MODULE` | `web` | (Set automatically) informs `start.sh` to run the web module. |

### API application (`Project_demo_API`)

| Variable | Default | Description |
|----------|---------|-------------|
| `PORT` | `8085` | Port exposed by Railway (set automatically to `8080` when deployed). |
| `JDBC_URL` / `DATABASE_URL` | `jdbc:h2:mem:project_demo_api;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1` | JDBC connection string. Overrides the values below when provided. |
| `DB_USERNAME` / `DATABASE_USERNAME` | `sa` | Database username. |
| `DB_PASSWORD` / `DATABASE_PASSWORD` | _(empty)_ | Database password. |
| `JDBC_DRIVER` / `DATABASE_DRIVER` | `org.h2.Driver` | Fully qualified JDBC driver class name. |
| `HIBERNATE_DIALECT` / `DATABASE_DIALECT` | `org.hibernate.dialect.H2Dialect` | Hibernate SQL dialect. |
| `SPRING_JPA_DDL_AUTO` | `update` | JPA schema management strategy. |
| `SPRING_JPA_SHOW_SQL` | `true` | Enables SQL logging when set to `true`. |
| `SPRING_JPA_FORMAT_SQL` | `true` | Formats SQL logging when set to `true`. |
| `SERVICE_MODULE` | `api` | (Set automatically) informs `start.sh` to run the API module. |

The API now defaults to an in-memory H2 database that is compatible with the existing JPA
entities. When using Railway's MySQL or PostgreSQL plugins, provide the JDBC connection
information and (optionally) override the driver and dialect via environment variables so the
API can connect to the managed database.

## Running locally with Docker

Each module contains a multi-stage `Dockerfile` that produces a slim runtime image:

```bash
# Build and run the web module
cd Project_demo
docker build -t project-demo-web .
docker run --rm -p 8082:8080 -e PORT=8080 -e STAFF_API_BASE_URL=http://host.docker.internal:8085/api/staff project-demo-web

# Build and run the API module
cd ../Project_demo_API
docker build -t project-demo-api .
docker run --rm -p 8085:8080 -e PORT=8080 project-demo-api
```

Adjust the environment variables as needed for your database and cross-service communication.

### Railway service ports

Railway always binds container processes to port `8080` inside the service. Both Spring Boot
applications respect the `PORT` environment variable, so no additional configuration is required
— simply leave the **Generate Service Domain** port at `8080` for both services. The defaults in
`application.properties` (`8082` for the web app and `8085` for the API) only apply when the
`PORT` variable is absent, such as when running the JAR directly on a developer workstation.
