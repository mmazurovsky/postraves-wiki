#!/bin/sh

cd ./app
gradle flywayMigrate
gralde jooqGenerate
