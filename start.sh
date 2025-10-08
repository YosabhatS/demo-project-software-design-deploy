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
    DEFAULT_JAR="Project_demo-0.0.1-SNAPSHOT.jar"
    ;;
  api)
    MODULE_DIR="Project_demo_API"
    DEFAULT_JAR="lab07_2567_one_many-0.0.1-SNAPSHOT.jar"
    ;;
  *)
    echo "Unknown service selection '$SERVICE_HINT'. Set SERVICE_MODULE to 'web' or 'api'." >&2
    exit 1
    ;;
esac

cd "$MODULE_DIR"

MVN_CMD=""

if [ -x "./mvnw" ] && [ -f "./.mvn/wrapper/maven-wrapper.properties" ]; then
  MVN_CMD="./mvnw"
elif command -v mvn >/dev/null 2>&1; then
  MVN_CMD="mvn"
else
  MAVEN_VERSION="${MAVEN_VERSION:-3.9.9}"
  MAVEN_CACHE_DIR=".railway-maven"
  MAVEN_BIN="$MAVEN_CACHE_DIR/bin/mvn"
  if [ ! -x "$MAVEN_BIN" ]; then
    echo "Maven not found. Downloading Apache Maven $MAVEN_VERSION..."
    rm -rf "$MAVEN_CACHE_DIR"
    mkdir -p "$MAVEN_CACHE_DIR"
    ARCHIVE="apache-maven-$MAVEN_VERSION-bin.tar.gz"
    CURL_OPTS="-fsSL"
    if command -v curl >/dev/null 2>&1; then
      curl $CURL_OPTS "https://dlcdn.apache.org/maven/maven-3/$MAVEN_VERSION/binaries/$ARCHIVE" -o "$MAVEN_CACHE_DIR/$ARCHIVE" \
        || curl $CURL_OPTS "https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/$MAVEN_VERSION/$ARCHIVE" -o "$MAVEN_CACHE_DIR/$ARCHIVE"
    elif command -v wget >/dev/null 2>&1; then
      wget -q "https://dlcdn.apache.org/maven/maven-3/$MAVEN_VERSION/binaries/$ARCHIVE" -O "$MAVEN_CACHE_DIR/$ARCHIVE" \
        || wget -q "https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/$MAVEN_VERSION/$ARCHIVE" -O "$MAVEN_CACHE_DIR/$ARCHIVE"
    else
      echo "Neither curl nor wget is available to download Maven." >&2
      exit 1
    fi
    tar -xzf "$MAVEN_CACHE_DIR/$ARCHIVE" -C "$MAVEN_CACHE_DIR"
    EXTRACT_DIR=$(find "$MAVEN_CACHE_DIR" -maxdepth 1 -type d -name 'apache-maven-*' | head -n1 || true)
    if [ -z "$EXTRACT_DIR" ]; then
      echo "Failed to extract Maven archive." >&2
      exit 1
    fi
    for ITEM in "$EXTRACT_DIR"/* "$EXTRACT_DIR"/.*; do
      case "$(basename "$ITEM")" in
        .|..)
          continue
          ;;
      esac
      mv "$ITEM" "$MAVEN_CACHE_DIR/"
    done
    rm -rf "$MAVEN_CACHE_DIR/$ARCHIVE" "$EXTRACT_DIR"
    chmod +x "$MAVEN_BIN"
  fi
  MVN_CMD="$MAVEN_BIN"
fi

JAR_PATH=""
if [ -f "target/$DEFAULT_JAR" ]; then
  JAR_PATH="target/$DEFAULT_JAR"
else
  CANDIDATE=$(find target -maxdepth 1 -type f -name '*.jar' ! -name 'original-*.jar' 2>/dev/null | head -n1 || true)
  if [ -n "$CANDIDATE" ]; then
    JAR_PATH="$CANDIDATE"
  fi
fi

if [ -z "$JAR_PATH" ]; then
  if [ -z "$MVN_CMD" ]; then
    echo "Maven is required to build the project but could not be located." >&2
    exit 1
  fi
  echo "Building $MODULE_DIR with Maven..."
  "$MVN_CMD" -B -DskipTests package
  JAR_PATH=$(find target -maxdepth 1 -type f -name '*.jar' ! -name 'original-*.jar' | head -n1 || true)
fi

if [ -z "$JAR_PATH" ]; then
  echo "Unable to locate the packaged JAR file after building $MODULE_DIR." >&2
  exit 1
fi

echo "Starting $MODULE_DIR from $JAR_PATH on port $PORT_VALUE..."
exec java -Dserver.port="$PORT_VALUE" -jar "$JAR_PATH"
