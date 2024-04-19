#!/bin/bash

# Format FLY's database URI. It uses 'postgres' instead of 'postgresql' in driver URL. Rewrite
# > postgres://USER:PASSWORD@HOST:PORT/DATABASE
# to
# > jdbc:postgresql://HOST/DATABASE?user=USER&password=PASSWORD

export DB_URI=$(echo "$DATABASE_URL" | sed -r "s/postgres:\/\/([^:]*):([^@]*)@([^:]*):(.*)/jdbc:postgresql\:\/\/\3:\4\?user=\1\&password=\2/g")
