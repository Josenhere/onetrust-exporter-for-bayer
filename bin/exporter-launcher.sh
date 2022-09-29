#!/bin/bash

TIMESTAMP=`date +%y-%m-%d_%H-%M-%S`
LOG_FILE=exporter_$TIMESTAMP.log
PROCESS="java -jar onetrust-exporter.jar"

cd ~exporter/exporter/
RUNNING_PROCESSES=`ps -aef | grep "$PROCESS" | grep -v grep | wc -l`
echo found $RUNNING_PROCESSES running processes
if [ $RUNNING_PROCESSES -eq 0 ];
then
        echo starting background process
        $PROCESS > $LOG_FILE 2>&1 &
else
        echo process is still running, doing nothing
fi