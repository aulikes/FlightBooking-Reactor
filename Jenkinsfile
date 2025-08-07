pipeline {
  agent any

  environment {
    DOCKER_IMAGE = 'flightbooking-api'
    IMAGE_TAG = 'latest'
    NAMESPACE = 'flightbooking-dev'
    TIMESTAMP = "${new Date().format('yyyyMMddHHmmss')}"
  }

  stages {

    stage('Checkout') {
      steps {
        checkout scm
      }
    }

    stage('Cleanup Gradle Locks') {
      steps {
        sh 'rm -rf /root/.gradle/caches/journal-1/journal-1.lock || true'
        sh 'rm -rf /root/.gradle/daemon/8.5/registry.bin.lock || true'
        sh 'pkill -f gradle || true'
        sh 'pkill -f java || true'
      }
    }

    stage('Build & Test') {
      steps {
        sh './gradlew clean build jacocoTestReport bootJar --no-daemon'
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

    stage('Build Docker Image (no rebuild)') {
      steps {
        script {
          sh '''
            eval "$(minikube docker-env)"
            docker build -t ${DOCKER_IMAGE}:${IMAGE_TAG} .
          '''
        }
      }
    }

    stage('Create Namespace') {
      steps {
        withCredentials([file(credentialsId: 'kubeconfig-jenkins', variable: 'KUBECONFIG')]) {
          sh '''
            if ! kubectl get namespace "$NAMESPACE" >/dev/null 2>&1; then
              kubectl create namespace "$NAMESPACE"
            fi
          '''
        }
      }
    }

    stage('Deploy to Kubernetes') {
      steps {
        withCredentials([file(credentialsId: 'kubeconfig-jenkins', variable: 'KUBECONFIG')]) {
          dir('k8/app') {
            sh '''
              sed -i "s/{{ timestamp }}/${TIMESTAMP}/g" 3-deployment-flightbooking.yaml
              kubectl apply -f 1-configmap-flightbooking.yaml -n ${NAMESPACE}
              kubectl apply -f 2-secret-flightbooking.yaml -n ${NAMESPACE}
              kubectl apply -f 3-deployment-flightbooking.yaml -n ${NAMESPACE}
              kubectl apply -f 4-service-flightbooking.yaml -n ${NAMESPACE}
            '''
          }
        }
      }
    }
  }
}
