#!/bin/bash

# Script de despliegue para el microservicio FlightBooking en Minikube de forma manual

# Namespace de Kubernetes donde se desplegará la aplicación
NAMESPACE="flightbooking-dev"

# Determinar dinámicamente la raíz del proyecto (dos niveles arriba desde la ubicación del script)
PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../../" && pwd)"

# Nombre de la imagen Docker a construir localmente
IMAGE_NAME="flightbooking:latest"

# Eliminar manifiestos anteriores para evitar conflictos
echo "Eliminando posibles recursos anteriores..."
kubectl delete -f 4-service-flightbooking.yaml -n "$NAMESPACE" --ignore-not-found
kubectl delete -f 3-deployment-flightbooking.yaml -n "$NAMESPACE" --ignore-not-found
kubectl delete -f 2-secret-flightbooking.yaml -n "$NAMESPACE" --ignore-not-found
kubectl delete -f 1-configmap-flightbooking.yaml -n "$NAMESPACE" --ignore-not-found

sleep 2

# Verificar existencia del namespace, y crearlo si no existe
echo "Verificando si el namespace '$NAMESPACE' existe..."
if ! kubectl get namespace "$NAMESPACE" >/dev/null 2>&1; then
  echo "Namespace no existe. Creando..."
  kubectl create namespace "$NAMESPACE"
else
  echo "Namespace '$NAMESPACE' ya existe."
fi


# Configurar entorno Docker de Minikube para construir la imagen localmente en su daemon
echo "Configurando entorno Docker de Minikube..."
eval "$(minikube docker-env)" || { echo "Error: No se pudo configurar el entorno Docker de Minikube"; exit 1; }

# Construcción de imagen Docker
echo "Construyendo imagen Docker local: $IMAGE_NAME"
cd "$PROJECT_DIR" || { echo "Error: No se pudo acceder al directorio del proyecto: $PROJECT_DIR"; exit 1; }

docker build -t "$IMAGE_NAME" . || { echo "Error: Falló la construcción de la imagen Docker"; exit 1; }

# Regresar al directorio original (donde están los manifiestos)
cd - >/dev/null

# Aplicar los manifiestos de Kubernetes
echo "Aplicando manifiestos YAML de Kubernetes..."
kubectl apply -f 1-configmap-flightbooking.yaml -n "$NAMESPACE"
kubectl apply -f 2-secret-flightbooking.yaml -n "$NAMESPACE"
kubectl apply -f 3-deployment-flightbooking.yaml -n "$NAMESPACE"
kubectl apply -f 4-service-flightbooking.yaml -n "$NAMESPACE"

# Mostrar logs de arranque
echo ""
echo "Verifica el estado con: kubectl get pods -n $NAMESPACE -l app=flightbooking"
echo "Verifica los logs con: kubectl logs -l app=flightbooking -n $NAMESPACE --tail=500"
