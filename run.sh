#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")"
if ! command -v mvn >/dev/null 2>&1; then
  echo 'Apache Maven no está disponible en PATH.' >&2
  exit 1
fi
mvn clean compile
mvn -q exec:java -Dexec.args="$*"
