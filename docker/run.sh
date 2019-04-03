#!/bin/bash

## build all images
docker-compose build --no-cache

## start source database and load balancer
docker-compose up --no-start
docker-compose start roach-0
docker-compose start roach-1
docker-compose start roach-2
docker-compose start lb

## wait for cockroach to initialize
sleep 5

## create backup database
docker-compose exec roach-0 /cockroach/cockroach sql --insecure --execute="CREATE DATABASE examples;"
docker-compose exec roach-0 /cockroach/cockroach sql --insecure --execute="SET CLUSTER SETTING server.remote_debugging.mode = \"any\";"
