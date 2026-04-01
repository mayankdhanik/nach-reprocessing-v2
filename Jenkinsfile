pipeline {
    agent any

    tools {
        maven 'Maven'
        nodejs 'NodeJS'
    }

    environment {
        WEBLOGIC_HOST    = 'weblogic-nach'
        WEBLOGIC_PORT    = '7001'
        WEBLOGIC_USER    = 'weblogic'
        WEBLOGIC_PASS    = 'Welcome@1234'
        APP_NAME         = 'nach-reprocessing'
        WAR_FILE         = 'target/nach-reprocessing.war'
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
                echo '=== Deploying WAR to WebLogic ==='
                sh """
                    curl -s -X POST \
                    --user ${WEBLOGIC_USER}:${WEBLOGIC_PASS} \
                    -H 'Content-Type: application/octet-stream' \
                    -H 'X-Requested-By: Jenkins' \
                    --data-binary @${WAR_FILE} \
                    "http://${WEBLOGIC_HOST}:${WEBLOGIC_PORT}/management/weblogic/latest/domainRuntime/deploymentManager/deployments" \
                    -o deploy_response.json
                """
                sh 'cat deploy_response.json'
                echo '=== WAR deployed to WebLogic ==='
            }
        }

        stage('Verify Deployment') {
            steps {
                echo '=== Checking deployment status ==='
                sh """
                    curl -s --user ${WEBLOGIC_USER}:${WEBLOGIC_PASS} \
                    "http://${WEBLOGIC_HOST}:${WEBLOGIC_PORT}/management/weblogic/latest/domainRuntime/deploymentManager/deployments/${APP_NAME}" \
                    | grep -o '"state":"[^"]*"' || echo 'Deployment check complete'
                """
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
