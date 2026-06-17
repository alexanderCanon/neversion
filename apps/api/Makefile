# =============================================================================
# NEVERSION - API (Spring Boot)
# Usage: make <target> [ARGS]
# =============================================================================

SHELL := /bin/bash
.SHELLFLAGS := -eu -o pipefail -c

.DEFAULT_GOAL := help

# =============================================================================
# HELP
# =============================================================================

.PHONY: help
help: ## Show all available commands
	@echo ""
	@echo "  NEVERSION API - Command Hub"
	@echo ""
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) \
		| awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-30s\033[0m %s\n", $$1, $$2}'
	@echo ""

# =============================================================================
# MAVEN
# =============================================================================

.PHONY: compile build test test-class test-method verify package clean run

compile: ## Compile source code only
	./mvnw compile

build: ## Full build (compile + test + package)
	./mvnw clean install

test: ## Run all tests
	./mvnw test

test-class: ## Run a single test class.  Usage: make test-class CLASS=CreateAccountServiceUT
	./mvnw test -Dtest=$(CLASS)

test-method: ## Run a single test method. Usage: make test-method CLASS=CreateAccountServiceUT METHOD=create_shouldReturnSavedAccount
	./mvnw test -Dtest=$(CLASS)#$(METHOD)

verify: ## Full verification including integration tests (Testcontainers)
	./mvnw verify

package: ## Package without running tests
	./mvnw clean package -DskipTests

clean: ## Clean build artifacts
	./mvnw clean

run: ## Run locally with Spring Boot dev tools (hot reload)
	./mvnw spring-boot:run

# =============================================================================
# DOCKER — DEV
# =============================================================================

.PHONY: dev-up dev-run dev-down dev-logs

dev-up: ## Start APi container (detached mode). Usage: make dev-up
	docker compose -f compose.local.yml up -d --build

dev-run:
	docker compose -f compose.local.yml up -d

dev-down: ## Stop dev stack
	docker compose -f compose.local.yml down

dev-logs: ## Follow dev container logs. Usage: make dev-logs | make dev-logs SERVICE=app
	@if [ -z "$(SERVICE)" ]; then \
		docker compose -f compose.local.yml logs -f; \
	else \
		docker compose -f compose.local.yml logs -f $(SERVICE); \
	fi
