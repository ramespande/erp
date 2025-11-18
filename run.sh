#!/usr/bin/env bash
set -euo pipefail

MAIN_CLASS="${MAIN_CLASS:-edu.univ.erp.Application}"

if [[ ! -d out/classes ]]; then
  echo "Classes not found. Run ./build.sh first." >&2
  exit 1
fi

CLASSPATH="out/classes"
if compgen -G "lib/*.jar" > /dev/null; then
  for jar in lib/*.jar; do
    CLASSPATH="$CLASSPATH:$jar"
  done
fi

java -cp "$CLASSPATH" "$MAIN_CLASS" "$@"

