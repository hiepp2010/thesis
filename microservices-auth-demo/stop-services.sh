#!/bin/bash

echo "Stopping all microservices..."

if [ -f service_pids.txt ]; then
    PIDS=$(cat service_pids.txt)
    echo "Killing processes with PIDs: $PIDS"
    kill $PIDS
    rm service_pids.txt
    echo "All services stopped."
else
    echo "No service PIDs found. Services may not be running."
fi 