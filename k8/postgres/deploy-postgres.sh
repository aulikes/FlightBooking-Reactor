#!/bin/bash

# Namespace donde se desplegarán los recursos
NAMESPACE="flightbooking-dev"

## Eliminar recursos previos en caso de errores anteriores
#echo "Eliminando posibles recursos anteriores..."
#kubectl delete -f 5-service-postgres.yaml -n "$NAMESPACE" --ignore-not-found
#kubectl delete -f 4-deployment-postgres.yaml -n "$NAMESPACE" --ignore-not-found
#kubectl delete -f 3-configmap-postgres.yaml -n "$NAMESPACE" --ignore-not-found
#kubectl delete -f 2-secret-postgres.yaml -n "$NAMESPACE" --ignore-not-found
#kubectl delete -f 1-pvc-postgres.yaml -n "$NAMESPACE" --ignore-not-found
#kubectl delete -f 0-pv-postgres.yaml --ignore-not-found
#
#sleep 2

# Verificar si el namespace existe
echo "Verificando si el namespace '$NAMESPACE' existe..."
if ! kubectl get namespace "$NAMESPACE" >/dev/null 2>&1; then
  echo "Namespace no existe. Creando..."
  kubectl create namespace "$NAMESPACE"
else
  echo "Namespace '$NAMESPACE' ya existe."
fi

# Aplicar todos los archivos YAML
echo "Aplicando manifiestos YAML de PostgreSQL..."
kubectl apply -f 0-pv-postgres.yaml
kubectl apply -f 1-pvc-postgres.yaml -n "$NAMESPACE"
kubectl apply -f 2-secret-postgres.yaml -n "$NAMESPACE"
kubectl apply -f 3-configmap-postgres.yaml -n "$NAMESPACE"
kubectl apply -f 4-deployment-postgres.yaml -n "$NAMESPACE"
kubectl apply -f 5-service-postgres.yaml -n "$NAMESPACE"


