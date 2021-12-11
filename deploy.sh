#!/bin/sh

docker buildx build --platform=linux/amd64 -t mmazurovsky/postraves-nginx ./nginx --no-cache
docker push mmazurovsky/postraves-nginx
docker-compose -f ./app/docker-compose-api.yml build
docker push mmazurovsky/postraves-wiki-api
eb deploy
