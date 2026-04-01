pipeline {
    agent any

    tools {
        maven 'Maven'
        nodejs 'NodeJS'
    }

    environment {
        APP_NAME = 'nach-reprocessing'
        WAR_FILE = 'target/nach-reprocessing.war'
    }

    stages {

        stage('Checkout') {
            steps {
                echo '=== Pulling latest code from GitHub ==='
                git branch: 'main',
                    url: 'https://github.com/mayankdhanik/nach-reprocessing-v2.git'
            }
        }

        stage('Build Backend') {
            steps {
                echo '=== Building Java WAR ==='
                sh 'mvn clean package -DskipTests'
                echo '=== WAR built successfully ==='
            }
        }

        stage('Build Frontend') {
            steps {
                echo '=== Installing npm dependencies ==='
                sh 'npm install'
                echo '=== Building React app ==='
                sh 'npm run build'
                echo '=== React build complete ==='
            }
        }

        stage('Deploy WAR to WebLogic') {
            steps {
                echo '=== Copying WAR to WebLogic autodeploy folder ==='
                sh 'docker cp ${WAR_FILE} weblogic-nach:/u01/oracle/user_projects/domains/NachDomain/autodeploy/'
                echo '=== WAR copied to WebLogic autodeploy ==='
            }
        }

        stage('Verify Deployment') {
            steps {
                echo '=== Waiting for WebLogic to deploy ==='
                sh 'sleep 15'
                sh 'docker exec weblogic-nach ls /u01/oracle/user_projects/domains/NachDomain/autodeploy/'
                echo '=== Deployment complete ==='
            }
        }
    }

    post {
        success {
            echo '========================================='
            echo '=== BUILD & DEPLOY SUCCESS ==='
            echo "=== App running at: http://localhost:7001/${APP_NAME} ==="
            echo '========================================='
        }
        failure {
            echo '========================================='
            echo '=== BUILD FAILED - Check logs above ==='
            echo '========================================='
        }
    }
}
