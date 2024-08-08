#!/bin/bash

set -e

echo "Starting all docker containers..."
docker-compose -f docker-compose.local.yml down

echo "Pruning unused Docker images..."
docker image prune -f

echo "Containers are up and running."
docker-compose ps -a
