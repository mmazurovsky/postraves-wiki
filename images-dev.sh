#!/bin/sh

set -e
docker buildx build --platform=linux/amd64 -t registry.digitalocean.com/postraves-registry/postraves-nginx-dev ./nginx --no-cache
docker push registry.digitalocean.com/postraves-registry/postraves-nginx-dev --all-tags
docker-compose -f ./app/docker-compose-api-dev.yml build
docker push registry.digitalocean.com/postraves-registry/postraves-wiki-api-dev
