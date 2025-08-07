#!/bin/bash

# Script de despliegue de Zookeeper en Kubernetes (entorno local)

NAMESPACE="flightbooking-dev"

# Eliminar recursos previos en caso de errores anteriores
echo "Eliminando recursos antiguos de Zookeeper si existen..."

kubectl delete -f 4-service-kafka.yaml -n "$NAMESPACE" --ignore-not-found
kubectl delete -f 3-deployment-kafka.yaml -n "$NAMESPACE" --ignore-not-found
kubectl delete -f 2-configmap-kafka.yaml -n "$NAMESPACE" --ignore-not-found
kubectl delete -f 1-pvc-kafka.yaml -n "$NAMESPACE" --ignore-not-found
kubectl delete -f 0-pv-kafka.yaml --ignore-not-found

kubectl delete -f 4-service-zookeeper.yaml -n "$NAMESPACE" --ignore-not-found
kubectl delete -f 3-deployment-zookeeper.yaml -n "$NAMESPACE" --ignore-not-found
kubectl delete -f 2-configmap-zookeeper.yaml -n "$NAMESPACE" --ignore-not-found
kubectl delete -f 1-pvc-zookeeper.yaml -n "$NAMESPACE" --ignore-not-found
kubectl delete -f 0-pv-zookeeper.yaml --ignore-not-found

sleep 2

# Verificar si el namespace existe
echo "Verificando si el namespace '$NAMESPACE' existe..."
if ! kubectl get namespace "$NAMESPACE" >/dev/null 2>&1; then
  echo "Namespace no existe. Creando..."
  kubectl create namespace "$NAMESPACE"
else
  echo "Namespace '$NAMESPACE' ya existe."
fi

# Aplicar todos los archivos YAML
echo "Aplicando manifiestos YAML para Zookeeper..."
kubectl apply -f 0-pv-zookeeper.yaml
kubectl apply -f 1-pvc-zookeeper.yaml -n "$NAMESPACE"
kubectl apply -f 2-configmap-zookeeper.yaml -n "$NAMESPACE"
kubectl apply -f 3-deployment-zookeeper.yaml -n "$NAMESPACE"
kubectl apply -f 4-service-zookeeper.yaml -n "$NAMESPACE"

echo "Esperando que Zookeeper esté listo..."
kubectl wait --for=condition=ready pod -l app=zookeeper -n "$NAMESPACE" --timeout=90s

kubectl apply -f 0-pv-kafka.yaml
kubectl apply -f 1-pvc-kafka.yaml -n "$NAMESPACE"
kubectl apply -f 2-configmap-kafka.yaml -n "$NAMESPACE"
kubectl apply -f 3-deployment-kafka.yaml -n "$NAMESPACE"
kubectl apply -f 4-service-kafka.yaml -n "$NAMESPACE"

echo ""
echo "Zookeeper y Kafka desplegados correctamente en el namespace '$NAMESPACE'."
echo "Verifica el estado con: kubectl get pods -n $NAMESPACE -l app=zookeeper"
