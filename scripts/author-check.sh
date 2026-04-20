#!/usr/bin/env bash
set -euo pipefail

VM_USER=elvis122545735
VM_HOST=136.111.138.221
SSH_KEY=${SSH_KEY:-$HOME/.ssh/id_ed25519}

echo ">> Triggering /api/author-check on VM (via SSH + loopback)..."
ssh -i "$SSH_KEY" "$VM_USER@$VM_HOST" \
  'curl -sS --max-time 180 http://127.0.0.1:8080/api/author-check'
echo
echo ">> done."
