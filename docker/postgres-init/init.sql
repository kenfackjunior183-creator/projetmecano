SELECT 'CREATE DATABASE mecano_auth_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'mecano_auth_db')\gexec
SELECT 'CREATE DATABASE mecano_users_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'mecano_users_db')\gexec
SELECT 'CREATE DATABASE mecano_geo_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'mecano_geo_db')\gexec
SELECT 'CREATE DATABASE mecano_billing_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'mecano_billing_db')\gexec
SELECT 'CREATE DATABASE mecano_repair_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'mecano_repair_db')\gexec
SELECT 'CREATE DATABASE mecano_notif_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'mecano_notif_db')\gexec
SELECT 'CREATE DATABASE mecano_admin_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'mecano_admin_db')\gexec
SELECT 'CREATE DATABASE mecano_marketplace_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'mecano_marketplace_db')\gexec
