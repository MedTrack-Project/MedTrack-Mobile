#!/usr/bin/env bash
set -euo pipefail

if [[ $# -ne 1 ]]; then
  echo "Uso: $0 <caminho-do-apk>" >&2
  exit 2
fi

apk_path="$1"
if [[ ! -f "$apk_path" ]]; then
  echo "APK não encontrado: $apk_path" >&2
  exit 2
fi

for tool in apksigner apkanalyzer sha256sum; do
  if ! command -v "$tool" >/dev/null 2>&1; then
    echo "Ferramenta obrigatória ausente no PATH: $tool" >&2
    exit 2
  fi
done

budget_file="$(dirname "$0")/../../config/release/budgets.properties"
max_bytes="$(sed -n 's/^maxApkSizeBytes=//p' "$budget_file")"
apk_bytes="$(stat -c '%s' "$apk_path")"
if (( apk_bytes > max_bytes )); then
  echo "APK excede o budget: ${apk_bytes}/${max_bytes} bytes" >&2
  exit 1
fi

apksigner verify --verbose "$apk_path"
version_name="$(apkanalyzer manifest version-name "$apk_path")"
version_code="$(apkanalyzer manifest version-code "$apk_path")"

apk_directory="$(dirname "$apk_path")"
apk_name="$(basename "$apk_path")"
(
  cd "$apk_directory"
  sha256sum "$apk_name" >"${apk_name}.sha256"
)

echo "APK verificado: versão ${version_name} (${version_code}), ${apk_bytes} bytes."
echo "Checksum: ${apk_path}.sha256"
