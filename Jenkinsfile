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
        // Libera posibles procesos o locks que bloquean Gradle
        sh 'rm -rf /root/.gradle/caches/journal-1/journal-1.lock || true'
        sh 'rm -rf /root/.gradle/daemon/8.5/registry.bin.lock || true'
        sh 'pkill -f gradle || true'
        sh 'pkill -f java || true'
      }
    }

//     stage('Build & Test') {
//       steps {
//         sh './gradlew clean build jacocoTestReport --no-daemon'
//       }
//     }
//
//     stage('SonarQube Analysis') {
//       steps {
//         withSonarQubeEnv('sonaqube-docker') {
//           withCredentials([string(credentialsId: 'Jenkins-Sonar', variable: 'SONAR_TOKEN')]) {
//             sh "./gradlew sonarqube -Dsonar.login=${SONAR_TOKEN} --info"
//           }
//         }
//       }
//     }
//
//     stage('Eliminar recursos previos') {
//       steps {
//         withCredentials([file(credentialsId: 'kubeconfig-jenkins', variable: 'KUBECONFIG')]) {
//             dir('k8/app') {
//               sh '''
//                 kubectl delete -f 4-service-flightbooking.yaml -n "$NAMESPACE" --ignore-not-found
//                 kubectl delete -f 3-deployment-flightbooking.yaml -n "$NAMESPACE" --ignore-not-found
//                 kubectl delete -f 2-secret-flightbooking.yaml -n "$NAMESPACE" --ignore-not-found
//                 kubectl delete -f 1-configmap-flightbooking.yaml -n "$NAMESPACE" --ignore-not-found
//               '''
//             }
//         }
//       }
//     }

    stage('Create Namespace if not exists') {
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

    stage('Build Docker Image for Minikube') {
      steps {
        script {
          sh '''
            eval "$(minikube docker-env)"
            docker build -t ${DOCKER_IMAGE}:${IMAGE_TAG} .
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

    stage('Wait for Pod Ready') {
      steps {
        withCredentials([file(credentialsId: 'kubeconfig-jenkins', variable: 'KUBECONFIG')]) {
          script {
            try {
              sh "kubectl wait --for=condition=ready pod -l app=flightbooking -n ${NAMESPACE} --timeout=180s"
            } catch (e) {
              echo 'El pod no estuvo listo en 180 segundos. Revisar manualmente.'
              sh "kubectl get pods -n ${NAMESPACE}"
              error('Pod no listo a tiempo.')
            }
          }
        }
      }
    }

    stage('Show Initial Logs') {
      steps {
        withCredentials([file(credentialsId: 'kubeconfig-jenkins', variable: 'KUBECONFIG')]) {
          echo "Verifica el estado con: kubectl get pods -n $NAMESPACE -l app=flightbooking"
          echo "Verifica los logs con: kubectl logs POD_NAME -n $NAMESPACE --tail=500"

          script {
            def podName = sh(
              script: "kubectl get pods -n ${NAMESPACE} -l app=flightbooking -o jsonpath='{.items[0].metadata.name}'",
              returnStdout: true
            ).trim()

            echo "Verifica los logs con: kubectl logs ${podName} -n $NAMESPACE --tail=500"
            echo "Mostrando logs del pod: ${podName}"
            sh "kubectl logs ${podName} -n ${NAMESPACE} --tail=100"
          }
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
