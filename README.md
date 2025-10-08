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
   packaged JAR is not already available. No additional build commands are required.
4. Configure environment variables for each service as described below.

## Environment variables

### Web application (`Project_demo`)

| Variable | Default | Description |
|----------|---------|-------------|
| `PORT` | `8082` | Port exposed by Railway (set automatically). |
| `STAFF_API_BASE_URL` | `http://localhost:8085/api/staff` | Public URL of the API service. |
| `STAFF_API_TIMEOUT` | `5s` | Client timeout when calling the API. |
| `SERVICE_MODULE` | `web` | (Set automatically) informs `start.sh` to run the web module. |

### API application (`Project_demo_API`)

| Variable | Default | Description |
|----------|---------|-------------|
| `PORT` | `8085` | Port exposed by Railway (set automatically). |
| `JDBC_URL` | `jdbc:mysql://localhost:3306/mydb_demo` | JDBC connection string. Overrides the values below when provided. |
| `MYSQL_HOST` | `localhost` | Database host used when `JDBC_URL` is not provided. |
| `MYSQL_PORT` | `3306` | Database port used when `JDBC_URL` is not provided. |
| `MYSQL_DATABASE` | `mydb_demo` | Database name used when `JDBC_URL` is not provided. |
| `DB_USERNAME` / `MYSQL_USER` | `user01` | Database username. |
| `DB_PASSWORD` / `MYSQL_PASSWORD` | `s$crete01` | Database password. |
| `JDBC_DRIVER` | `com.mysql.cj.jdbc.Driver` | Fully qualified JDBC driver class name. |
| `HIBERNATE_DIALECT` | `org.hibernate.dialect.MySQL8Dialect` | Hibernate SQL dialect. |
| `SPRING_JPA_DDL_AUTO` | `update` | JPA schema management strategy. |
| `SPRING_JPA_SHOW_SQL` | `true` | Enables SQL logging when set to `true`. |
| `SPRING_JPA_FORMAT_SQL` | `true` | Formats SQL logging when set to `true`. |
| `SERVICE_MODULE` | `api` | (Set automatically) informs `start.sh` to run the API module. |

When using Railway's MySQL or PostgreSQL plugins, copy the generated credentials into the
matching environment variables (or provide a JDBC URL) so that the API can connect to the
managed database.

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
