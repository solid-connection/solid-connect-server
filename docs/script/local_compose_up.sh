#!/bin/bash

# 명령이 0이 아닌 종료값을 가질때 즉시 종료
set -e

BASE_DIR="$(cd "$(dirname "$0")/../.." && pwd)"

if [ ! -d "${BASE_DIR}/mysql_data_local" ]; then
  echo "mysql_data_local 디렉토리가 없습니다. ${BASE_DIR}/mysql_data_local 디렉토리를 생성합니다."
  mkdir -p "${BASE_DIR}/mysql_data_local"
fi

if [ ! -d "${BASE_DIR}/redis_data_local" ]; then
  echo "redis_data_local 디렉토리가 없습니다. ${BASE_DIR}/redis_data_local 디렉토리를 생성합니다."
  mkdir -p "${BASE_DIR}/redis_data_local"
fi

echo "Starting all docker containers..."
docker compose -f "${BASE_DIR}/docker-compose.local.yml" up -d

echo "Pruning unused Docker images..."
docker image prune -f

echo "Containers are up and running."
docker compose ps -a
