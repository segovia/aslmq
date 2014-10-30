#!/bin/bash
ant dist
./bash/amazonMiddlewareDeploy.sh 6 9
./bash/amazonClientDeploy.sh 10 13

