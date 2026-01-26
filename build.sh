#!/bin/bash

# Frontend
cd frontend
./build.sh --no-docker clean init build
cd ..

# Create directory structure and copy frontend dist
cd backend
find ./src/main/resources/public/ -maxdepth 1 -type f -exec rm -f {} +

cp -R ../frontend/dist-home/* ./src/main/resources/public/

# Build backend
./build.sh --no-docker clean build

# Clean up - remove frontend/dist-home
rm -rf ../frontend/dist-home
