#!/usr/bin/env groovy

pipeline {
    agent any

    stages {
        stage('Initialization') {
            steps {
                script {
                    def version = sh(returnStdout: true, script: 'docker compose -f backend/docker-compose.yml run --rm maven mvn $MVN_OPTS help:evaluate -Dexpression=project.version -q -DforceStdout').trim()
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
                    sh 'cp -R ../frontend/dist-home/* ./src/main/resources/public/'
                    sh './build.sh init clean build publish'
                    sh 'rm -rf ../frontend/dist-home'
                }
            }
        }
    }

    post {
        cleanup {
            sh 'cd backend && (docker compose down || true) && cd ../frontend && (docker compose down || true)'
        }
    }
}