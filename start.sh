#!/bin/sh
set -euo pipefail

SERVICE_NAME="${RAILWAY_SERVICE_NAME:-${SERVICE_MODULE:-project_demo_web}}"
PORT_VALUE="${PORT:-8080}"

case "$SERVICE_NAME" in
  project_demo_web|Project_demo|web|WEB)
    MODULE_DIR="Project_demo"
    JAR_NAME="Project_demo-0.0.1-SNAPSHOT.jar"
    ;;
  project_demo_api|Project_demo_API|api|API)
    MODULE_DIR="Project_demo_API"
    JAR_NAME="lab07_2567_one_many-0.0.1-SNAPSHOT.jar"
    ;;
  *)
    echo "Unknown service name '$SERVICE_NAME'. Set SERVICE_MODULE to 'web' or 'api'." >&2
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
