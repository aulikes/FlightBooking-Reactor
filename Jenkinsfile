pipeline {
  agent any

  environment {
    // Variables necesarias para Sonar y Gradle
    DOCKER_IMAGE = 'flightbooking-api'
    DOCKER_PORT = '8095'
    SPRING_PROFILE = 'dev'
    DOCKER_NETWORK = 'flightbooking-net'
  }

  stages {

    stage('Checkout') {
      steps {
        checkout scm
      }
    }

    stage('Cleanup Gradle Locks') {
      steps {
        // Libera posibles procesos o locks que bloquean Gradle
        sh 'rm -rf /root/.gradle/caches/journal-1/journal-1.lock || true'
        sh 'rm -rf /root/.gradle/daemon/8.5/registry.bin.lock || true'
        sh 'pkill -f gradle || true'
        sh 'pkill -f java || true'
      }
    }

    stage('Build & Test') {
      steps {
        sh './gradlew clean build jacocoTestReport --no-daemon'
      }
    }

    stage('SonarQube Analysis') {
      steps {
        withSonarQubeEnv('sonaqube-docker') {
          withCredentials([string(credentialsId: 'Jenkins-Sonar', variable: 'SONAR_TOKEN')]) {
            sh "./gradlew sonarqube -Dsonar.login=${SONAR_TOKEN} --info"
          }
        }
      }
    }

    stage('Build Docker Image') {
      steps {
        script {
          sh "docker build -t ${DOCKER_IMAGE} ."
        }
      }
    }

    stage('Run Container') {
      steps {
        script {
          // Stop previous container if running
          sh "docker rm -f ${DOCKER_IMAGE} || true"
          // Run new container on port 8090
          sh "docker run -d \
            --network ${DOCKER_NETWORK} \
            -e SPRING_PROFILE=${SPRING_PROFILE} \
            -p ${DOCKER_PORT}:${DOCKER_PORT} \
            --name ${DOCKER_IMAGE} ${DOCKER_IMAGE}"
//           sh "docker run -d -e SPRING_PROFILE=${SPRING_PROFILE} -p ${DOCKER_PORT}:${DOCKER_PORT} --name ${DOCKER_IMAGE} ${DOCKER_IMAGE} -Dsonar.scm.disabled=true"
        }
      }
    }
  }

  post {
    failure {
      echo 'Pipeline failed.'
    }
    success {
      echo 'Pipeline completed successfully.'
    }
  }
}
