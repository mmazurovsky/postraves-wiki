#!/bin/sh

set -e
docker buildx build --platform=linux/amd64 -t registry.digitalocean.com/postraves-registry/postraves-nginx-dev ./nginx --no-cache
docker push registry.digitalocean.com/postraves-registry/postraves-nginx-dev
docker-compose -f ./app/docker-compose-api.yml --env-file .env-dev build
docker push registry.digitalocean.com/postraves-registry/postraves-wiki-api-dev
