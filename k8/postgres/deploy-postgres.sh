#!/bin/bash

# Namespace donde se desplegarán los recursos
NAMESPACE="flightbooking-dev"

# Eliminar recursos previos en caso de errores anteriores
echo "Eliminando posibles recursos anteriores..."
kubectl delete -f 5-service-postgres.yaml -n "$NAMESPACE" --ignore-not-found
kubectl delete -f 4-deployment-postgres.yaml -n "$NAMESPACE" --ignore-not-found
kubectl delete -f 3-configmap-postgres.yaml -n "$NAMESPACE" --ignore-not-found
kubectl delete -f 2-secret-postgres.yaml -n "$NAMESPACE" --ignore-not-found
kubectl delete -f 1-pvc-postgres.yaml -n "$NAMESPACE" --ignore-not-found
kubectl delete -f 0-pv-postgres.yaml --ignore-not-found

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
echo "Aplicando manifiestos YAML de PostgreSQL..."
kubectl apply -f 0-pv-postgres.yaml
kubectl apply -f 1-pvc-postgres.yaml -n "$NAMESPACE"
kubectl apply -f 2-secret-postgres.yaml -n "$NAMESPACE"
kubectl apply -f 3-configmap-postgres.yaml -n "$NAMESPACE"
kubectl apply -f 4-deployment-postgres.yaml -n "$NAMESPACE"
kubectl apply -f 5-service-postgres.yaml -n "$NAMESPACE"

# Decodificar credenciales desde el Secret
echo ""
echo "Verificando credenciales decodificadas:"
USER=$(kubectl get secret postgres-secret -n "$NAMESPACE" -o jsonpath="{.data.postgres-user}" | base64 --decode)
PASS=$(kubectl get secret postgres-secret -n "$NAMESPACE" -o jsonpath="{.data.postgres-password}" | base64 --decode)

# Obtener nombre de base de datos desde el ConfigMap
DBNAME=$(kubectl get configmap postgres-config -n "$NAMESPACE" -o jsonpath="{.data.DB_NAME}")

# Obtener el puerto NodePort asignado dinámicamente
PORT=$(kubectl get service postgres-service -n "$NAMESPACE" -o jsonpath="{.spec.ports[0].nodePort}")

# Mostrar información de acceso
echo ""
echo "PostgreSQL desplegado correctamente en el namespace '$NAMESPACE'."
echo "Accede desde tu host con los siguientes datos:"
echo "  Host: localhost"
echo "  Puerto: $PORT"
echo "  Usuario: $USER"
echo "  Contraseña: $PASS"
echo "  Base de Datos: $DBNAME"
