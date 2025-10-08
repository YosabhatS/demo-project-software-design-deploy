#!/bin/sh
set -euo pipefail

SERVICE_NAME="${RAILWAY_SERVICE_NAME:-${SERVICE_MODULE:-project_demo_web}}"
PORT_VALUE="${PORT:-8080}"

# Determine which module should run. Allow explicit overrides via SERVICE_MODULE, but
# fall back to heuristics that examine the (case-insensitive) Railway service name so
# that names such as "project-demo-api" or "prod-web" work out-of-the-box.
SERVICE_HINT="${SERVICE_MODULE:-}"
if [ -z "$SERVICE_HINT" ]; then
  SERVICE_HINT=$(printf '%s' "$SERVICE_NAME" | tr '[:upper:]' '[:lower:]')
  case "$SERVICE_HINT" in
    *api*)
      SERVICE_HINT="api"
      ;;
    *web*|*front*|*ui*)
      SERVICE_HINT="web"
      ;;
    *)
      SERVICE_HINT="web"
      ;;
  esac
fi

case "$SERVICE_HINT" in
  web)
    MODULE_DIR="Project_demo"
    JAR_NAME="Project_demo-0.0.1-SNAPSHOT.jar"
    ;;
  api)
    MODULE_DIR="Project_demo_API"
    JAR_NAME="lab07_2567_one_many-0.0.1-SNAPSHOT.jar"
    ;;
  *)
    echo "Unknown service selection '$SERVICE_HINT'. Set SERVICE_MODULE to 'web' or 'api'." >&2
    exit 1
    ;;
esac

cd "$MODULE_DIR"

MVN_CMD="mvn"
if [ -x "./mvnw" ] && [ -f "./.mvn/wrapper/maven-wrapper.properties" ]; then
  MVN_CMD="./mvnw"
fi

if [ ! -f "target/$JAR_NAME" ]; then
  echo "Building $MODULE_DIR with Maven..."
  "$MVN_CMD" -B -DskipTests package
fi

echo "Starting $MODULE_DIR on port $PORT_VALUE..."
exec java "-Dserver.port=$PORT_VALUE" -jar "target/$JAR_NAME"
