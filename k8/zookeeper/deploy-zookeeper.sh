#!/bin/bash

# Script de despliegue de Zookeeper en Kubernetes (entorno local)
# Producción:
# - Se recomienda usar Helm o Kustomize para gestionar múltiples entornos
# - Las rutas locales (hostPath) deben ser reemplazadas por almacenamiento dinámico
# - NodePort debe cambiarse por ClusterIP o LoadBalancer según el caso

NAMESPACE="flightbooking-dev"

echo "Eliminando recursos antiguos de Zookeeper si existen..."
kubectl delete -f 4-service-zookeeper.yaml -n "$NAMESPACE" --ignore-not-found
kubectl delete -f 3-deployment-zookeeper.yaml -n "$NAMESPACE" --ignore-not-found
kubectl delete -f 2-configmap-zookeeper.yaml -n "$NAMESPACE" --ignore-not-found
kubectl delete -f 1-pvc-zookeeper.yaml -n "$NAMESPACE" --ignore-not-found
kubectl delete -f 0-pv-zookeeper.yaml --ignore-not-found

sleep 2

echo "Verificando si el namespace '$NAMESPACE' existe..."
if ! kubectl get namespace "$NAMESPACE" >/dev/null 2>&1; then
  echo "Namespace no existe. Creando..."
  kubectl create namespace "$NAMESPACE"
else
  echo "Namespace '$NAMESPACE' ya existe."
fi

echo "Desplegando recursos YAML para Zookeeper..."
kubectl apply -f 0-pv-zookeeper.yaml
kubectl apply -f 1-pvc-zookeeper.yaml -n "$NAMESPACE"
kubectl apply -f 2-configmap-zookeeper.yaml -n "$NAMESPACE"
kubectl apply -f 3-deployment-zookeeper.yaml -n "$NAMESPACE"
kubectl apply -f 4-service-zookeeper.yaml -n "$NAMESPACE"

echo ""
echo "Zookeeper desplegado correctamente en el namespace '$NAMESPACE'."
echo "Verifica el estado con: kubectl get pods -n $NAMESPACE -l app=zookeeper"
