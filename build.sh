#!/bin/bash

# If DEBUG env var is set to "true" then set -x to enable debug mode
if [ "$DEBUG" == "true" ]; then
	set -x
	EDIFICE_CLI_DEBUG_OPTION="--debug"
else
	EDIFICE_CLI_DEBUG_OPTION=""
fi

init() {
  me=`id -u`:`id -g`
  echo "DEFAULT_DOCKER_USER=$me" > .env

  # If CLI_VERSION is empty set to latest
  if [ -z "$CLI_VERSION" ]; then
    CLI_VERSION="latest"
  fi
  # Create a build.compose.yaml file from following template
  cat <<EOF > build.compose.yaml
services:
  edifice-cli:
    image: opendigitaleducation/edifice-cli:$CLI_VERSION
    user: "$DEFAULT_DOCKER_USER"
EOF
  # Copy /root/edifice from edifice-cli container to host machine
  docker compose -f build.compose.yaml create edifice-cli
  docker compose -f build.compose.yaml cp edifice-cli:/root/edifice ./edifice
  docker compose -f build.compose.yaml rm -fsv edifice-cli
  rm -f build.compose.yaml
  chmod +x edifice
  ./edifice version $EDIFICE_CLI_DEBUG_OPTION
}

# If called without arguments, run the full local build
if [ "$#" -eq 0 ]; then

  # Frontend
  cd frontend
  ./build.sh --no-docker clean init build
  cd ..

  # Create directory structure and copy frontend dist
  cd backend
  find ./src/main/resources/public/ -maxdepth 1 -type f -exec rm -f {} +


  # Build backend
  ./build.sh --no-docker clean build
fi


if [ ! -e .env ]; then
  init
fi

buildNode () {
  case `uname -s` in
    MINGW*)
      docker compose run --rm -u "$USER_UID:$GROUP_GID" node sh -c "npm install --no-bin-links && node_modules/gulp/bin/gulp.js build"
      ;;
    *)
      docker compose run --rm -u "$USER_UID:$GROUP_GID" node sh -c "npm install && node_modules/gulp/bin/gulp.js build"
  esac
}

buildReact () {
  echo "Building React frontend..."
  cd frontend
  case `uname -s` in
    MINGW*)
      docker-compose run --rm -u "$USER_UID:$GROUP_GID" node sh -c "pnpm install --no-bin-links && pnpm build"
      ;;
    *)
      docker-compose run --rm -u "$USER_UID:$GROUP_GID" node sh -c "pnpm install && pnpm build"
  esac
  cd ..
  
  # Copy React build to public directory (only JS and CSS, not HTML)
  echo "Copying React build to public directory..."
  mkdir -p src/main/resources/public/dist-home
  cp frontend/dist-home/*.js src/main/resources/public/dist-home/ 2>/dev/null || true
  cp frontend/dist-home/*.css src/main/resources/public/dist-home/ 2>/dev/null || true
  echo "React build completed and copied successfully"
}

install () {
  docker compose run --rm maven mvn $MVN_OPTS install -DskipTests
}

testNode () {
  rm -rf coverage
  rm -rf */build
  case `uname -s` in
    MINGW*)
      docker compose run --rm -u "$USER_UID:$GROUP_GID" node sh -c "npm install --no-bin-links && node_modules/gulp/bin/gulp.js drop-cache &&  npm test"
      ;;
    *)
      docker compose run --rm -u "$USER_UID:$GROUP_GID" node sh -c "npm install && node_modules/gulp/bin/gulp.js drop-cache && npm test"
  esac
}

testNodeDev () {
  rm -rf coverage
  rm -rf */build
  case `uname -s` in
    MINGW*)
      docker compose run --rm -u "$USER_UID:$GROUP_GID" node sh -c "npm install --no-bin-links && node_modules/gulp/bin/gulp.js drop-cache &&  npm run test:dev"
      ;;
    *)
      docker compose run --rm -u "$USER_UID:$GROUP_GID" node sh -c "npm install && node_modules/gulp/bin/gulp.js drop-cache && npm run test:dev"
  esac
}

test() {
  docker compose run --rm maven mvn $MVN_OPTS test
}

publish() {
    version=`docker compose run --rm maven mvn $MVN_OPTS help:evaluate -Dexpression=project.version -q -DforceStdout`
    level=`echo $version | cut -d'-' -f3`
    case "$level" in
        *SNAPSHOT)
            export nexusRepository='snapshots'
            ;;
        *)
            export nexusRepository='releases'
            ;;
    esac
    docker compose run --rm maven mvn $MVN_OPTS -DrepositoryId=ode-$nexusRepository -DskiptTests -Dmaven.test.skip=true --settings /var/maven/.m2/settings.xml deploy
}

publishNexus() {
  version=`docker compose run --rm maven mvn $MVN_OPTS help:evaluate -Dexpression=project.version -q -DforceStdout`
  level=`echo $version | cut -d'-' -f3`
  case "$level" in
    *SNAPSHOT) export nexusRepository='snapshots' ;;
    *)         export nexusRepository='releases' ;;
  esac
  docker compose run --rm  maven mvn -DrepositoryId=ode-$nexusRepository -Durl=$repo -DskipTests -Dmaven.test.skip=true --settings /var/maven/.m2/settings.xml deploy
}

for param in "$@"
do
  case $param in
    clean)
      clean
      ;;
    init)
      init
      ;;
    buildNode)
      buildNode
      ;;
    buildReact)
      buildReact
      ;;
    buildMaven)
      install
      ;;
    install)
      buildNode && buildReact && install
      ;;
    publish)
      publish
      ;;
    publishNexus)
      publishNexus
      ;;
    test)
      testNode ; test
      ;;
    testNode)
      testNode
      ;;
    testNodeDev)
      testNodeDev
      ;;
    test)
      test
      ;;
    *)
      echo "Invalid argument : $param"
  esac
  if [ ! $? -eq 0 ]; then
    exit 1
  fi
done
