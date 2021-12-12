#!/bin/sh

cd ./app
gradle flywayMigrate
gradle generateJooq
