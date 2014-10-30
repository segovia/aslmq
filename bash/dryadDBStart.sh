#!/bin/bash
ssh gsegovia@dryad09.ethz.ch  "/mnt/local/gsegovia/postgres/bin/postgres -D /mnt/local/gsegovia/postgres/db/ -i -p 40001 -k /mnt/local/gsegovia/ > /mnt/local/gsegovia/logfile 2>&1 &"