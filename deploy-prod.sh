#!/bin/sh

docker buildx build --platform=linux/amd64 -t mmazurovsky/postraves-nginx-prod ./nginx --no-cache
docker push mmazurovsky/postraves-nginx-prod
docker-compose -f ./app/docker-compose-api.yml --env-file .env-prod build
docker push mmazurovsky/postraves-wiki-api-prod
eb deploy
