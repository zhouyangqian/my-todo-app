#!/bin/bash
# Deploy all services to Kubernetes
# Usage: ./k8s/deploy.sh

set -e

echo "=== Deploying My Todo App to Kubernetes ==="

# Apply namespace first
kubectl apply -f k8s/namespace.yaml

# Apply infrastructure (MySQL, Redis, Nacos)
echo ">>> Deploying infrastructure..."
kubectl apply -f k8s/mysql.yaml
kubectl apply -f k8s/redis.yaml
kubectl apply -f k8s/services.yaml

# Wait for MySQL to be ready
echo ">>> Waiting for MySQL..."
kubectl wait --for=condition=ready pod -l app=mysql -n my-todo --timeout=120s

# Wait for Redis to be ready
echo ">>> Waiting for Redis..."
kubectl wait --for=condition=ready pod -l app=redis -n my-todo --timeout=60s

# Wait for Nacos to be ready
echo ">>> Waiting for Nacos..."
kubectl wait --for=condition=ready pod -l app=nacos -n my-todo --timeout=120s

# Apply microservices
echo ">>> Deploying microservices..."
kubectl apply -f k8s/microservices.yaml

echo ">>> Waiting for all deployments..."
kubectl wait --for=condition=available deployment --all -n my-todo --timeout=300s

echo "=== Deployment complete ==="
echo ""
echo "Check status: kubectl get pods -n my-todo"
echo "Port forward: kubectl port-forward svc/frontend 3000:80 -n my-todo"
