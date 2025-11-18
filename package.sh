#!/usr/bin/env bash
set -euo pipefail

MAIN_CLASS="${MAIN_CLASS:-edu.univ.erp.Application}"

./build.sh

DIST_DIR="dist/erp-app"
rm -rf "$DIST_DIR"
mkdir -p "$DIST_DIR/lib"

cp -R out/classes "$DIST_DIR/classes"

if compgen -G "lib/*.jar" > /dev/null; then
  cp lib/*.jar "$DIST_DIR/lib/" 2>/dev/null || true
fi

cat > "$DIST_DIR/run.sh" <<'EOF'
#!/usr/bin/env bash
BASE_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CLASSPATH="$BASE_DIR/classes"
for jar in "$BASE_DIR"/lib/*.jar; do
  CLASSPATH="$CLASSPATH:$jar"
done
MAIN_CLASS="\${MAIN_CLASS:-edu.univ.erp.Application}"
java -cp "$CLASSPATH" "$MAIN_CLASS" "$@"
EOF
chmod +x "$DIST_DIR/run.sh"

cat > "$DIST_DIR/run.bat" <<'EOF'
@echo off
setlocal enabledelayedexpansion
set BASE_DIR=%~dp0
set CLASSPATH=%BASE_DIR%classes
for %%f in (%BASE_DIR%lib\*.jar) do set CLASSPATH=!CLASSPATH!;%%f
if "%MAIN_CLASS%"=="" set MAIN_CLASS=edu.univ.erp.Application
java -cp "%CLASSPATH%" %MAIN_CLASS% %*
EOF

jar --create --file dist/erp-app.jar --main-class "$MAIN_CLASS" -C out/classes .

echo "Distribution created under dist/erp-app and dist/erp-app.jar"

