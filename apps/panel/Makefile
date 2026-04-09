# =============================================================================
# NEVERSION - Panel (Angular 17)
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
	@echo "  NEVERSION Panel - Command Hub"
	@echo ""
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) \
		| awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-30s\033[0m %s\n", $$1, $$2}'
	@echo ""

# =============================================================================
# DEPENDENCIES
# =============================================================================

.PHONY: install

install: ## Install dependencies with pnpm
	pnpm install

# =============================================================================
# DEVELOPMENT
# =============================================================================

.PHONY: start watch

start: ## Start dev server at http://localhost:4200
	pnpm start

watch: ## Build in watch mode (development config)
	pnpm run watch

# =============================================================================
# BUILD
# =============================================================================

.PHONY: build

build: ## Build for production
	pnpm run build

# =============================================================================
# TEST
# =============================================================================

.PHONY: test test-file

test: ## Run all tests (Karma + Jasmine)
	pnpm test

test-file: ## Run a single spec file. Usage: make test-file SPEC=auth.service.spec.ts
	pnpm exec ng test --include="**/$(SPEC)"

# =============================================================================
# ANGULAR CLI — GENERATE
# =============================================================================

.PHONY: gen-component gen-service gen-guard

gen-component: ## Generate standalone component. Usage: make gen-component FEATURE=products NAME=product-list
	pnpm exec ng generate component features/$(FEATURE)/$(NAME) --standalone

gen-service: ## Generate service in core/services. Usage: make gen-service NAME=product
	pnpm exec ng generate service core/services/$(NAME)

gen-guard: ## Generate functional guard in core/guards. Usage: make gen-guard NAME=auth
	pnpm exec ng generate guard core/guards/$(NAME) --functional

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

git-commit: ## Stage all changes and commit. Usage: make git-commit MSG="feat: add dashboard component"
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
