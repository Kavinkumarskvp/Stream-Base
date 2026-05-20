#!/bin/bash
set -e

echo "Waiting for primary to be ready..."
until pg_isready -h postgres-primary -U myuser; do
  sleep 1
done

if [ -z "$(ls -A $PGDATA)" ]; then
  echo "Running initial base backup from primary..."
  PGPASSWORD=replicator_secret pg_basebackup \
    -h postgres-primary \
    -U replicator \
    -D $PGDATA \
    -Fp -Xs -R -P
  chown -R postgres:postgres $PGDATA
  echo "Base backup complete. Starting replica..."
else
  echo "Data directory exists. Starting replica..."
fi

exec /usr/local/bin/docker-entrypoint.sh postgres