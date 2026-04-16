#!/usr/bin/env groovy

pipeline {
    agent any

    environment {
        NPM_TOKEN = credentials('npm-token')
        TIPTAP_PRO_TOKEN = credentials('tiptap-pro-token')
    }

    stages {
        stage('Initialization') {
            steps {
                script {
                    sh './build.sh init'
                    def version = sh(returnStdout: true, script: 'docker run --rm -u `id -u`:`id -g` --env MAVEN_CONFIG=/var/maven/.m2 -w /usr/src/maven -v ./backend/:/usr/src/maven -v ~/.m2:/var/maven/.m2  opendigitaleducation/mvn-java8-node20:latest mvn -Duser.home=/var/maven help:evaluate -Dexpression=project.version -DforceStdout -q')
                    buildName "${env.GIT_BRANCH.replace('origin/', '')}@${version}"
                }
            }
        }

        stage('Frontend') {
            steps {
                script {
                    parallel(
                        'React Frontend': {
                            dir('frontend') {
                                sh './build.sh clean init build'
                            }
                        },
                        'Old Node Frontend': {
                            dir('backend') {
                                sh './build.sh buildNode'
                            }
                        }
                    )
                }
            }
        }

        stage('Backend') {
            steps {
                dir('backend') {
                    sh 'mkdir -p ./src/main/resources/public/ || true'
                    sh 'find ./src/main/resources/public/ -maxdepth 1 -type f -exec rm -f {} +'
                    sh 'cp -R ../frontend/dist-home/public/* ./src/main/resources/public/'
                    sh 'cp -R ../frontend/dist-home/*.js ./src/main/resources/public/'
                    sh './build.sh init clean build publish'
                    sh 'rm -rf ../frontend/dist-home'
                }
            }
        }
        stage('Build image') {
            steps {
            sh './edifice image --rebuild=false'
            }
        }
    }

    post {
        cleanup {
            sh 'cd backend && (docker compose down || true) && cd ../frontend && (docker compose down || true)'
        }
    }
}