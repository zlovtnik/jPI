# Makefile for build & deploy operations for jPI
# Usage: edit IMAGE and optionally NAMESPACE or set env vars when invoking make.

IMAGE ?= ghcr.io/${GITHUB_OWNER:-your-org}/jpi
TAG ?= $(shell git rev-parse --short HEAD 2>/dev/null || echo local)
NAMESPACE ?= default
KUBE_CONFIG_PATH ?= $(HOME)/.kube/config

.PHONY: help build push helm-render helm-deploy helm-lint decode-kubeconfig uninstall

help:
	@echo "Available targets:"
	@echo "  make build             # build Docker image locally"
	@echo "  make push              # build and push image to registry"
	@echo "  make helm-render       # render Helm templates"
	@echo "  make helm-deploy       # push image (if needed) and helm upgrade --install"
	@echo "  make helm-lint         # helm lint the chart"
	@echo "  make decode-kubeconfig # decode KUBE_CONFIG_DATA (base64) into $(KUBE_CONFIG_PATH)"
	@echo "  make uninstall         # helm uninstall release"

# Build the docker image locally
build:
	@echo "Building image: $(IMAGE):$(TAG)"
	docker build -t $(IMAGE):$(TAG) .

# Push the image to the registry (build first)
push: build
	@echo "Pushing image: $(IMAGE):$(TAG)"
	docker push $(IMAGE):$(TAG)

# Render Helm templates (dry-run output)
helm-render:
	helm template jpi charts/jpi --set image.repository=$(IMAGE) --set image.tag=$(TAG)

# Lint the helm chart
helm-lint:
	helm lint charts/jpi

# Decode KUBE_CONFIG_DATA secret into a kubeconfig file used for helm operations
decode-kubeconfig:
	@if [ -n "$(KUBE_CONFIG_DATA)" ]; then \
		echo "Decoding KUBE_CONFIG_DATA to $(KUBE_CONFIG_PATH)"; \
		echo "$(KUBE_CONFIG_DATA)" | base64 --decode > $(KUBE_CONFIG_PATH); \
		echo "Wrote $(KUBE_CONFIG_PATH)"; \
	else \
		echo "KUBE_CONFIG_DATA is empty; set it to the base64-encoded kubeconfig to use this target."; \
	fi

# Deploy the chart with helm (will decode KUBE_CONFIG_DATA if provided)
helm-deploy: push
	@echo "Preparing kubeconfig..."
	@if [ -n "$(KUBE_CONFIG_DATA)" ]; then \
		echo "Decoding KUBE_CONFIG_DATA to $(KUBE_CONFIG_PATH)"; \
		echo "$(KUBE_CONFIG_DATA)" | base64 --decode > $(KUBE_CONFIG_PATH); \
	fi
	@echo "Running: helm upgrade --install jpi charts/jpi --namespace $(NAMESPACE) --set image.repository=$(IMAGE) --set image.tag=$(TAG)"
	helm upgrade --install jpi charts/jpi --namespace $(NAMESPACE) --create-namespace --set image.repository=$(IMAGE) --set image.tag=$(TAG)

# Uninstall release
uninstall:
	@echo "Uninstalling release jpi from namespace $(NAMESPACE)"
	helm uninstall jpi --namespace $(NAMESPACE) || true
