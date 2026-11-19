#!/bin/bash
set -euo pipefail
# Só roda com volume vazio. Flyway é responsável pelas tabelas e migrações.
psql --username "$POSTGRES_USER" --dbname postgres --set ON_ERROR_STOP=1 \
  --set payment_user="$PAYMENT_DB_USER" --set payment_pass="$PAYMENT_DB_PASSWORD" \
  --set receipt_user="$RECEIPT_DB_USER" --set receipt_pass="$RECEIPT_DB_PASSWORD" <<'SQL'
CREATE USER :"payment_user" WITH PASSWORD :'payment_pass';
CREATE USER :"receipt_user" WITH PASSWORD :'receipt_pass';
CREATE DATABASE paymentdb OWNER :"payment_user";
CREATE DATABASE receiptdb OWNER :"receipt_user";
REVOKE CONNECT ON DATABASE paymentdb FROM PUBLIC;
REVOKE CONNECT ON DATABASE receiptdb FROM PUBLIC;
SQL
