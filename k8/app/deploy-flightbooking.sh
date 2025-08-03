#!/bin/bash

# Nombre del namespace para el despliegue
NAMESPACE="flightbooking-dev"

# Ruta al directorio donde se encuentra el Dockerfile (ajústalo si es necesario)
PROJECT_DIR="../../FlightBooking-reactor"

# Nombre de la imagen a construir y usar localmente
IMAGE_NAME="flightbooking:latest"

# Activar el entorno Docker de Minikube para usar su daemon local
echo "Configurando entorno Docker de Minikube..."
eval $(minikube docker-env)

# Eliminar recursos anteriores si existen
echo "Eliminando posibles recursos anteriores..."
kubectl delete -f 4-service-flightbooking.yaml -n "$NAMESPACE" --ignore-not-found
kubectl delete -f 3-deployment-flightbooking.yaml -n "$NAMESPACE" --ignore-not-found
kubectl delete -f 2-secret-flightbooking.yaml -n "$NAMESPACE" --ignore-not-found
kubectl delete -f 1-configmap-flightbooking.yaml -n "$NAMESPACE" --ignore-not-found

sleep 2

# Verificar si el namespace existe
echo "Verificando si el namespace '$NAMESPACE' existe..."
if ! kubectl get namespace "$NAMESPACE" >/dev/null 2>&1; then
  echo "Namespace no existe. Creando..."
  kubectl create namespace "$NAMESPACE"
else
  echo "Namespace '$NAMESPACE' ya existe."
fi

# Construir la imagen local
echo "Construyendo imagen Docker local: $IMAGE_NAME"
cd "$PROJECT_DIR" || { echo "No se pudo acceder al directorio del proyecto"; exit 1; }
docker build -t "$IMAGE_NAME" .

# Volver al directorio donde están los manifiestos
cd - >/dev/null

# Aplicar los manifiestos de Kubernetes
echo "Aplicando manifiestos YAML..."
kubectl apply -f 1-configmap-flightbooking.yaml -n "$NAMESPACE"
kubectl apply -f 2-secret-flightbooking.yaml -n "$NAMESPACE"
kubectl apply -f 3-deployment-flightbooking.yaml -n "$NAMESPACE"
kubectl apply -f 4-service-flightbooking.yaml -n "$NAMESPACE"

# Verificar estado del pod
echo ""
echo "Esperando a que el pod esté en estado Running..."
kubectl wait --for=condition=ready pod -l app=flightbooking -n "$NAMESPACE" --timeout=60s

# Mostrar logs iniciales
echo ""
echo "Logs de la aplicación:"
kubectl logs -l app=flightbooking -n "$NAMESPACE" --tail=50
