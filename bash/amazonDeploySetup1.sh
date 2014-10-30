#!/bin/bash
ant dist
./bash/amazonMiddlewareDeploy.sh 4 5
./bash/amazonClientDeploy.sh 6 7

