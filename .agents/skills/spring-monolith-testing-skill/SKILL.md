---
name: spring-monolith-testing-skill
description: Specialized Spring skill for testing strategy in the Neversion API. Use when the task is about test planning, context extraction, or production-oriented test layers for the modular monolith.
---

# Spring Monolith Testing Skill

Use this skill only for test strategy and test planning in `apps/api`.

## Scope

- Spring Boot monoliths
- Hexagonal / modular monolith architectures
- Production-oriented testing context

## What it does

- Extracts test-relevant architecture context
- Defines MVP test layers
- Identifies domain, application, repository, controller, security, and critical E2E test surfaces
- Helps produce phased testing plans and reusable test artifacts

## What it does not do

- It does not replace the main Spring standards skill
- It does not define Spring coding style or architecture baseline
- It does not apply to frontend work

## Minimum test layers

- Domain unit tests
- Application or use case tests
- Repository integration tests
- Controller or API tests
- Security tests
- Critical end-to-end tests

## Operating rule

Use this skill only when the task is about tests, not when implementing regular Spring features.
