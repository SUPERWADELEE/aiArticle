#!/usr/bin/env bash
set -euo pipefail

VM_USER=elvis122545735
VM_HOST=136.111.138.221
SSH_KEY=${SSH_KEY:-$HOME/.ssh/id_ed25519}

run_remote() {
  local label="$1" path="$2"
  echo ">> [$label] POST $path ..."
  ssh -i "$SSH_KEY" "$VM_USER@$VM_HOST" \
    "curl -sS --max-time 300 http://127.0.0.1:8080${path}"
  echo
  echo ">> [$label] done."
  echo
}

run_remote "fetch"         "/api/fetch"
run_remote "author-check"  "/api/author-check"
