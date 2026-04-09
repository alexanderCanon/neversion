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

.PHONY: dev-up dev-down dev-logs

dev-up: ## Start dev stack — PostgreSQL + app (detached). Usage: make dev-up
	docker compose -f compose.dev.yaml up -d --build

dev-down: ## Stop dev stack
	docker compose -f compose.dev.yaml down

dev-logs: ## Follow dev container logs. Usage: make dev-logs | make dev-logs SERVICE=app
	@if [ -z "$(SERVICE)" ]; then \
		docker compose -f compose.dev.yaml logs -f; \
	else \
		docker compose -f compose.dev.yaml logs -f $(SERVICE); \
	fi

# =============================================================================
# DOCKER — PROD
# =============================================================================

.PHONY: up down logs build-image

up: ## Start production stack (detached). Usage: make up
	docker compose -f compose.prod.yaml up -d --build

down: ## Stop and remove production containers
	docker compose -f compose.prod.yaml down

logs: ## Follow production logs. Usage: make logs | make logs SERVICE=app
	@if [ -z "$(SERVICE)" ]; then \
		docker compose -f compose.prod.yaml logs -f; \
	else \
		docker compose -f compose.prod.yaml logs -f $(SERVICE); \
	fi

build-image: ## Build Docker image only (no compose)
	docker compose -f compose.prod.yaml build

# =============================================================================
# GIT
# =============================================================================

.PHONY: git-status git-log git-pull git-commit git-push git-branch git-new-branch git-tag git-push-tag git-stash git-stash-pop

git-status: ## Show working tree status
	git status

git-log: ## Show last 20 commits (graph + decorations)
	git log --oneline --graph --decorate -20

git-pull: ## Pull latest changes (rebase). Usage: make git-pull | make git-pull BRANCH=main
	@if [ -z "$(BRANCH)" ]; then \
		git pull --rebase; \
	else \
		git pull --rebase origin $(BRANCH); \
	fi

git-commit: ## Stage all changes and commit. Usage: make git-commit MSG="feat: add endpoint"
	git add -A && git commit -m "$(MSG)"

git-push: ## Push current branch to origin. Usage: make git-push | make git-push BRANCH=feature/xyz
	@CURRENT=$$(git rev-parse --abbrev-ref HEAD); \
	TARGET=$${BRANCH:-$$CURRENT}; \
	git push origin $$TARGET

git-branch: ## List all branches (local + remote)
	git branch -a

git-new-branch: ## Create and switch to a new branch. Usage: make git-new-branch BRANCH=feature/my-feature
	git checkout -b $(BRANCH)

git-tag: ## Create an annotated release tag. Usage: make git-tag TAG=v1.2.0 MSG="Release 1.2.0"
	git tag -a $(TAG) -m "$(MSG)"

git-push-tag: ## Push a tag to origin. Usage: make git-push-tag TAG=v1.2.0
	git push origin $(TAG)

git-stash: ## Stash uncommitted changes. Usage: make git-stash | make git-stash MSG="wip: refactor"
	@if [ -z "$(MSG)" ]; then \
		git stash; \
	else \
		git stash push -m "$(MSG)"; \
	fi

git-stash-pop: ## Apply the latest stash and remove it
	git stash pop
