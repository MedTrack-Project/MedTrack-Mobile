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

resolve_android_tool() {
  local tool="$1"
  if command -v "$tool" >/dev/null 2>&1; then
    command -v "$tool"
    return
  fi
  if [[ -n "${ANDROID_HOME:-}" ]]; then
    find "$ANDROID_HOME" -type f -name "$tool" -perm -u+x 2>/dev/null | sort -V | tail -n 1
  fi
}

apksigner_path="$(resolve_android_tool apksigner)"
apkanalyzer_path="$(resolve_android_tool apkanalyzer)"
if [[ -z "$apksigner_path" || -z "$apkanalyzer_path" ]] || ! command -v sha256sum >/dev/null 2>&1; then
  echo "apksigner, apkanalyzer e sha256sum são obrigatórios." >&2
  exit 2
fi

budget_file="$(dirname "$0")/../../config/release/budgets.properties"
max_bytes="$(sed -n 's/^maxApkSizeBytes=//p' "$budget_file")"
apk_bytes="$(stat -c '%s' "$apk_path")"
if (( apk_bytes > max_bytes )); then
  echo "APK excede o budget: ${apk_bytes}/${max_bytes} bytes" >&2
  exit 1
fi

"$apksigner_path" verify --verbose "$apk_path"
version_name="$("$apkanalyzer_path" manifest version-name "$apk_path")"
version_code="$("$apkanalyzer_path" manifest version-code "$apk_path")"

apk_directory="$(dirname "$apk_path")"
apk_name="$(basename "$apk_path")"
(
  cd "$apk_directory"
  sha256sum "$apk_name" >"${apk_name}.sha256"
)

echo "APK verificado: versão ${version_name} (${version_code}), ${apk_bytes} bytes."
echo "Checksum: ${apk_path}.sha256"
