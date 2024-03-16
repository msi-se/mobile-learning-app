#!/bin/bash

# Kill all running screens with the name mobile-learning-frontend and mobile-learning-backend
screen -ls | grep mobile-learning-frontend | cut -d. -f1 | awk '{print $1}' | xargs kill
screen -ls | grep mobile-learning-backend | cut -d. -f1 | awk '{print $1}' | xargs kill

# Run the flutter web app and the quarkus backend and use screen to keep them running
screen -dm -S mobile-learning-frontend bash -c 'cd frontend; flutter run -d chrome'
screen -dm -S mobile-learning-backend bash -c 'cd backend; ./mvnw quarkus:dev'

# List all running screens
screen -ls