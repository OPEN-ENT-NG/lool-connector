#!/bin/bash

MVN_OPTS="-Duser.home=/var/maven"

# Params
NO_DOCKER=""
for i in "$@"
do
case $i in
  --no-docker*)
  NO_DOCKER="true"
  shift
  ;;
  *)
  ;;
esac
done

case `uname -s` in
  MINGW* | Darwin*)
    USER_UID=1000
    GROUP_UID=1000
    ;;
  *)
    if [ -z ${USER_UID:+x} ]
    then
      USER_UID=`id -u`
      GROUP_GID=`id -g`
    fi
esac

init() {
  me=`id -u`:`id -g`
  echo "DEFAULT_DOCKER_USER=$me" > .env
}

# Clean backend
clean() {
  echo "Cleaning backend..."
  if [ "$NO_DOCKER" = "true" ] ; then
    mvn clean
  else
    docker compose run --rm maven mvn $MVN_OPTS clean
  fi
  echo "Backend clean done!"
}

# Build old Node frontend (Gulp-based)
buildNode () {
  echo "Building old Node frontend..."
  case `uname -s` in
    MINGW*)
      docker compose run --rm -u "$USER_UID:$GROUP_GID" node sh -c "npm install --no-bin-links && node_modules/gulp/bin/gulp.js build"
      ;;
    *)
      docker compose run --rm -u "$USER_UID:$GROUP_GID" node sh -c "npm install && node_modules/gulp/bin/gulp.js build"
  esac
  echo "Old Node frontend build done!"
}

# Build backend
build() {
  echo "Building backend..."
  if [ "$NO_DOCKER" = "true" ] ; then
    mvn install -DskipTests
  else
    docker compose run --rm maven mvn $MVN_OPTS install -DskipTests
  fi
  echo "Backend build done!"
}

# Test backend
test() {
  echo "Testing backend..."
  if [ "$NO_DOCKER" = "true" ] ; then
    mvn test
  else
    docker compose run --rm maven mvn $MVN_OPTS test
  fi
  echo "Backend tests done!"
}

# Publish backend
publish() {
  if [ "$NO_DOCKER" = "true" ] ; then
    version=`mvn help:evaluate -Dexpression=project.version -q -DforceStdout`
  else
    version=`docker compose run --rm maven mvn $MVN_OPTS help:evaluate -Dexpression=project.version -q -DforceStdout`
  fi
  level=`echo $version | cut -d'-' -f3`
  case "$level" in
    *SNAPSHOT) export nexusRepository='snapshots' ;;
    *)         export nexusRepository='releases' ;;
  esac
  
  if [ "$NO_DOCKER" = "true" ] ; then
    mvn -DrepositoryId=ode-$nexusRepository -DskipTests -Dmaven.test.skip=true --settings ~/.m2/settings.xml deploy
  else
    docker compose run --rm maven mvn -DrepositoryId=ode-$nexusRepository -DskipTests -Dmaven.test.skip=true --settings /var/maven/.m2/settings.xml deploy
  fi
  echo "Backend publish done!"
}

# Execute commands
for param in "$@"
do
  case $param in
    --no-docker)
      # Skip, already handled
      ;;
    init)
      init
      ;;
    clean)
      clean
      ;;
    buildNode)
      buildNode
      ;;
    build)
      build
      ;;
    test)
      test
      ;;
    publish)
      publish
      ;;
    *)
      echo "Invalid argument : $param"
      echo "Usage: $0 [--no-docker] <init|clean|buildNode|build|test|publish>"
      exit 1
  esac
  if [ ! $? -eq 0 ]; then
    exit 1
  fi
done
