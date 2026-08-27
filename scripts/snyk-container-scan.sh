#!/usr/bin/env bash
set -euo pipefail

readonly SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
readonly REPO_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
readonly DEFAULT_IMAGE_NAME="laa-data-claims-notify-service:local"
readonly GRADLE_TASK=":laa-data-claims-notify-service:bootBuildImage"

image_name="${DEFAULT_IMAGE_NAME}"
severity_threshold="${SNYK_SEVERITY_THRESHOLD:-high}"
skip_build=0
extra_snyk_args=()

usage() {
  cat <<'EOF'
Usage: ./scripts/snyk-container-scan.sh [options] [-- <extra snyk args>]

Builds the service image locally with Gradle bootBuildImage and runs a Snyk container scan.

Options:
  --image-name <name>            Docker image name to build and scan
                                 (default: laa-data-claims-notify-service:local)
  --severity-threshold <level>   low | medium | high | critical
                                 (default: high, or SNYK_SEVERITY_THRESHOLD)
  --skip-build                   Scan an existing local image without rebuilding it
  -h, --help                     Show this help text

Authentication:
  The script supports the same Snyk credentials used in CI when exported locally:
    SNYK_TOKEN
    SNYK_CLIENT_ID + SNYK_CLIENT_SECRET

Examples:
  ./scripts/snyk-container-scan.sh
  ./scripts/snyk-container-scan.sh --image-name laa-notify:test --severity-threshold critical
  ./scripts/snyk-container-scan.sh --skip-build -- --json
EOF
}

require_command() {
  local command_name="$1"

  if ! command -v "${command_name}" >/dev/null 2>&1; then
    echo "Missing required command: ${command_name}" >&2
    exit 1
  fi
}

authenticate_snyk() {
  if [[ -n "${SNYK_TOKEN:-}" ]]; then
    snyk auth "${SNYK_TOKEN}" >/dev/null
  elif [[ -n "${SNYK_CLIENT_ID:-}" || -n "${SNYK_CLIENT_SECRET:-}" ]]; then
    if [[ -z "${SNYK_CLIENT_ID:-}" || -z "${SNYK_CLIENT_SECRET:-}" ]]; then
      echo "Both SNYK_CLIENT_ID and SNYK_CLIENT_SECRET must be set together." >&2
      exit 1
    fi

    snyk auth --auth-type=oauth \
      --client-id="${SNYK_CLIENT_ID}" \
      --client-secret="${SNYK_CLIENT_SECRET}" >/dev/null
  elif ! snyk whoami >/dev/null 2>&1; then
    echo "Snyk CLI is not authenticated or the saved session has expired." >&2
    echo "Run 'snyk auth' or export SNYK_TOKEN or SNYK_CLIENT_ID and SNYK_CLIENT_SECRET." >&2
    exit 1
  fi

  if ! snyk whoami >/dev/null 2>&1; then
    echo "Snyk authentication failed." >&2
    echo "Re-run 'snyk auth' or refresh SNYK_TOKEN / SNYK_CLIENT_ID / SNYK_CLIENT_SECRET." >&2
    exit 1
  fi
}

run_snyk_container_test() {
  if [[ "${#extra_snyk_args[@]}" -gt 0 ]]; then
    snyk container test "${image_name}" \
      --policy-path=.snyk \
      --severity-threshold="${severity_threshold}" \
      "${extra_snyk_args[@]}"
    return
  fi

  snyk container test "${image_name}" \
    --policy-path=.snyk \
    --severity-threshold="${severity_threshold}"
}

while (($# > 0)); do
  case "$1" in
    --image-name)
      image_name="${2:?Missing value for --image-name}"
      shift 2
      ;;
    --severity-threshold)
      severity_threshold="${2:?Missing value for --severity-threshold}"
      shift 2
      ;;
    --skip-build)
      skip_build=1
      shift
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    --)
      shift
      extra_snyk_args=("$@")
      break
      ;;
    *)
      echo "Unknown argument: $1" >&2
      usage >&2
      exit 1
      ;;
  esac
done

case "${severity_threshold}" in
  low|medium|high|critical) ;;
  *)
    echo "Invalid severity threshold: ${severity_threshold}" >&2
    echo "Expected one of: low, medium, high, critical" >&2
    exit 1
    ;;
esac

require_command docker
require_command snyk

if [[ ! -x "${REPO_ROOT}/gradlew" ]]; then
  echo "Gradle wrapper not found or not executable at ${REPO_ROOT}/gradlew" >&2
  exit 1
fi

if [[ "${skip_build}" -eq 0 ]]; then
  (
    cd "${REPO_ROOT}"
    ./gradlew "${GRADLE_TASK}" "--imageName=${image_name}"
  )
fi

authenticate_snyk

(
  cd "${REPO_ROOT}"
  run_snyk_container_test
)
