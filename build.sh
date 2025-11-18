#!/usr/bin/env bash
set -euo pipefail

MAIN_CLASS="${MAIN_CLASS:-edu.univ.erp.Application}"

SRC_MAIN="src/main/java"
if [[ ! -d "$SRC_MAIN" ]]; then
  SRC_MAIN="src"
fi

mkdir -p out/classes

CLASSPATH="out/classes"
if compgen -G "lib/*.jar" > /dev/null; then
  for jar in lib/*.jar; do
    CLASSPATH="$CLASSPATH:$jar"
  done
fi

find "$SRC_MAIN" -name "*.java" > sources-main.txt

if [[ ! -s sources-main.txt ]]; then
  echo "No Java sources found under $SRC_MAIN" >&2
  rm sources-main.txt
  exit 1
fi

javac -d out/classes -classpath "$CLASSPATH" @"sources-main.txt"
rm sources-main.txt

echo "Compiled application classes into out/classes"
echo "Next: set MAIN_CLASS (currently $MAIN_CLASS) and run ./run.sh or run.bat"

