# =============================================================================
# NEVERSION - ROOT COMMAND HUB
# Usage: make <target> [ARGS]
# =============================================================================
 
# -- Shell configuration ------------------------------------------------------
# Forces all commands to run in bash (WSL Ubuntu), not sh or PowerShell
SHELL := /bin/bash
.SHELLFLAGS := -eu -o pipefail -c
 
# -- Default target -----------------------------------------------------------
# Running bare `make` shows available commands
.DEFAULT_GOAL := help
 
# -- Project structure --------------------------------------------------------
API_DIR   := api
PANEL_DIR := panel
FRONT_DIR := front
DOCS_DIR  := docs
 
# =============================================================================
# HELP
# =============================================================================
 
.PHONY: help
help: ## Show all available commands
	@echo ""
	@echo "  NEVERSION - Command Hub"
	@echo ""
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) \
		| awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-30s\033[0m %s\n", $$1, $$2}'
	@echo ""
 
# =============================================================================
# GIT
# =============================================================================
 
.PHONY: git-status git-add git-commit git-push git-pull git-branch git-checkout git-log
 
git-status: ## Show git status
	git status
 
git-add: ## Stage all changes
	git add .
 
git-commit: ## Commit staged changes. Usage: make git-commit MSG="your message"
	@if [ -z "$(MSG)" ]; then \
		echo "Error: MSG is required. Usage: make git-commit MSG=\"your message\""; \
		exit 1; \
	fi
	git commit -m "$(MSG)"
 
git-push: ## Push to remote branch. Usage: make git-push BRANCH=main
	@if [ -z "$(BRANCH)" ]; then \
		echo "Error: BRANCH is required. Usage: make git-push BRANCH=main"; \
		exit 1; \
	fi
	git push origin $(BRANCH)
 
git-pull: ## Pull from remote branch. Usage: make git-pull BRANCH=main
	@if [ -z "$(BRANCH)" ]; then \
		echo "Error: BRANCH is required. Usage: make git-pull BRANCH=main"; \
		exit 1; \
	fi
	git pull origin $(BRANCH)
 
git-branch: ## Create and switch to a new branch. Usage: make git-branch NAME=feature/my-feature
	@if [ -z "$(NAME)" ]; then \
		echo "Error: NAME is required. Usage: make git-branch NAME=feature/my-feature"; \
		exit 1; \
	fi
	git checkout -b $(NAME)
 
git-checkout: ## Switch to an existing branch. Usage: make git-checkout BRANCH=main
	@if [ -z "$(BRANCH)" ]; then \
		echo "Error: BRANCH is required. Usage: make git-checkout BRANCH=main"; \
		exit 1; \
	fi
	git checkout $(BRANCH)
 
git-log: ## Show last 10 commits (one line each)
	git log --oneline -10
 
# =============================================================================
# FILESYSTEM (bash - no PowerShell)
# =============================================================================
 
.PHONY: find-file move-file delete-file tree
 
find-file: ## Find a file by name. Usage: make find-file NAME=MyClass.java
	@if [ -z "$(NAME)" ]; then \
		echo "Error: NAME is required. Usage: make find-file NAME=MyClass.java"; \
		exit 1; \
	fi
	find . -name "$(NAME)" -not -path "*/node_modules/*" -not -path "*/.git/*" -not -path "*/target/*"
 
move-file: ## Move a file or folder. Usage: make move-file SRC=old/path DST=new/path
	@if [ -z "$(SRC)" ] || [ -z "$(DST)" ]; then \
		echo "Error: SRC and DST are required. Usage: make move-file SRC=old/path DST=new/path"; \
		exit 1; \
	fi
	mv $(SRC) $(DST)
 
delete-file: ## Delete a file or folder (use with care). Usage: make delete-file PATH=some/path
	@if [ -z "$(PATH)" ]; then \
		echo "Error: PATH is required. Usage: make delete-file PATH=some/path"; \
		exit 1; \
	fi
	rm -rf $(PATH)
 
tree: ## Show project folder structure (2 levels deep)
	find . -maxdepth 2 \
		-not -path "*/.git/*" \
		-not -path "*/node_modules/*" \
		-not -path "*/target/*" \
		-not -path "*/.angular/*" \
		| sort | sed 's|[^/]*/|  |g'
 
# =============================================================================
# SCRIPTS (delegates to Python scripts for complex operations)
# =============================================================================
 
.PHONY: rename-module
 
rename-module: ## Rename a module across the project. Usage: make rename-module OLD=users NEW=clients
	@if [ -z "$(OLD)" ] || [ -z "$(NEW)" ]; then \
		echo "Error: OLD and NEW are required. Usage: make rename-module OLD=users NEW=clients"; \
		exit 1; \
	fi
	python3 scripts/rename_module.py $(OLD) $(NEW)
 
# =============================================================================
# ORCHESTRATION (delegates to subproject Makefiles)
# =============================================================================
 
.PHONY: build-all up-all down-all
 
build-all: ## Build all subprojects (api, panel, front)
	$(MAKE) -C $(API_DIR) build
	$(MAKE) -C $(PANEL_DIR) build
	$(MAKE) -C $(FRONT_DIR) build
 
up-all: ## Start full stack with Docker
	$(MAKE) -C $(API_DIR) up
 
down-all: ## Stop full stack with Docker
	$(MAKE) -C $(API_DIR) down