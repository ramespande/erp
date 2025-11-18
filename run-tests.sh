#!/usr/bin/env bash
set -euo pipefail

JUNIT_JAR="${JUNIT_JAR:-}"
if [[ -z "$JUNIT_JAR" ]]; then
  JUNIT_JAR=$(ls lib/junit-platform-console-standalone-*.jar 2>/dev/null | head -n1 || true)
fi

if [[ -z "$JUNIT_JAR" ]]; then
  echo "JUnit console jar not found. Place junit-platform-console-standalone-*.jar in lib/ or set JUNIT_JAR." >&2
  exit 1
fi

./build.sh

mkdir -p out/test-classes

CLASSPATH="out/classes"
if compgen -G "lib/*.jar" > /dev/null; then
  for jar in lib/*.jar; do
    CLASSPATH="$CLASSPATH:$jar"
  done
fi
CLASSPATH="$CLASSPATH:$JUNIT_JAR"

find src/test/java -name "*.java" > sources-test.txt

javac -d out/test-classes -classpath "$CLASSPATH" @sources-test.txt
rm sources-test.txt

TEST_CLASSPATH="out/test-classes:out/classes"
if compgen -G "lib/*.jar" > /dev/null; then
  for jar in lib/*.jar; do
    TEST_CLASSPATH="$TEST_CLASSPATH:$jar"
  done
fi

java -jar "$JUNIT_JAR" --class-path "$TEST_CLASSPATH" --scan-classpath

