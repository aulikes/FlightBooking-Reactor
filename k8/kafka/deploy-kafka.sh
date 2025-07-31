#!/bin/bash

# Script de despliegue de Kafka en Kubernetes (entorno local)

NAMESPACE="flightbooking-dev"

# Eliminar recursos previos en caso de errores anteriores
echo "Eliminando recursos antiguos de Kafka si existen..."
kubectl delete -f 4-service-kafka.yaml -n "$NAMESPACE" --ignore-not-found
kubectl delete -f 3-deployment-kafka.yaml -n "$NAMESPACE" --ignore-not-found
kubectl delete -f 2-configmap-kafka.yaml -n "$NAMESPACE" --ignore-not-found
kubectl delete -f 1-pvc-kafka.yaml -n "$NAMESPACE" --ignore-not-found
kubectl delete -f 0-pv-kafka.yaml --ignore-not-found

sleep 2

# Verificar si el namespace existe
echo "Verificando si el namespace '$NAMESPACE' existe..."
if ! kubectl get namespace "$NAMESPACE" >/dev/null 2>&1; then
  echo "Namespace no existe. Creando..."
  kubectl create namespace "$NAMESPACE"
else
  echo "Namespace '$NAMESPACE' ya existe."
fi

echo ""
echo "Aplicando manifiestos YAML para Kafka..."
kubectl apply -f 0-pv-kafka.yaml
kubectl apply -f 1-pvc-kafka.yaml -n "$NAMESPACE"
kubectl apply -f 2-configmap-kafka.yaml -n "$NAMESPACE"
kubectl apply -f 3-deployment-kafka.yaml -n "$NAMESPACE"
kubectl apply -f 4-service-kafka.yaml -n "$NAMESPACE"

echo ""
echo "Kafka desplegado correctamente en el namespace '$NAMESPACE'."
echo "Verifica con: kubectl get pods -n $NAMESPACE -l app=kafka"
