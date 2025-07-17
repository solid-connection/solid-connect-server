#!/bin/bash

set -e

echo "Stopping all docker containers..."
docker compose -f docker-compose.local.yml down

echo "Pruning unused Docker images..."
docker image prune -f

echo "Containers are down and not running."
docker compose ps -a
