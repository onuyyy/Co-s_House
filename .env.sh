#!/bin/bash
export $(grep -v '^#' .env | xargs)
./gradlew bootRun