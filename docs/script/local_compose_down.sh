#!/bin/bash

set -e

BASE_DIR="$(cd "$(dirname "$0")/../.." && pwd)"

echo "Stopping all docker containers..."
docker compose -f "${BASE_DIR}/docker-compose.local.yml" down

echo "Pruning unused Docker images..."
docker image prune -f

echo "Containers are down and not running."
docker compose ps -a
