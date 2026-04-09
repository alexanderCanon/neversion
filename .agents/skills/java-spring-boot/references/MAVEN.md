# Maven Wrapper – Quick Reference Guide

> Practical reference for Java projects with Spring Boot.  
> Covers **PowerShell**, **CMD (Windows 10/11)**, and **Ubuntu Server 24 LTS**.

---

## Command Differences by Environment

| Environment        | Base Command       | Note                                      |
|--------------------|--------------------|-------------------------------------------|
| PowerShell         | `.\mvnw`           | Requires `.\` to execute local scripts    |
| CMD                | `mvnw`             | No prefix or extension                     |
| Bash / Ubuntu 24   | `./mvnw`           | Requires execution permissions (`chmod`)  |

### First use on Ubuntu – Grant execution permissions

```bash
# Only the first time after cloning the repository
chmod +x mvnw
```

---

## Build and Compilation

| Description                               | PowerShell                            | CMD                                | Ubuntu / Bash                        |
|-------------------------------------------|---------------------------------------|------------------------------------|--------------------------------------|
| Compile (no tests)                        | `.\mvnw compile`                      | `mvnw compile`                     | `./mvnw compile`                     |
| Package JAR/WAR (with tests)              | `.\mvnw package`                      | `mvnw package`                     | `./mvnw package`                     |
| Package **without tests**                 | `.\mvnw package -DskipTests`          | `mvnw package -DskipTests`         | `./mvnw package -DskipTests`         |
| Clean `target/`                           | `.\mvnw clean`                        | `mvnw clean`                       | `./mvnw clean`                       |
| **Clean + package** (most used in CI/CD)  | `.\mvnw clean package`                | `mvnw clean package`               | `./mvnw clean package`               |
| Clean + package without tests             | `.\mvnw clean package -DskipTests`    | `mvnw clean package -DskipTests`   | `./mvnw clean package -DskipTests`   |

---

## Testing

| Description                            | PowerShell                                                       | CMD                                                           | Ubuntu / Bash                                                  |
|----------------------------------------|------------------------------------------------------------------|---------------------------------------------------------------|----------------------------------------------------------------|
| Run all tests                          | `.\mvnw test`                                                    | `mvnw test`                                                   | `./mvnw test`                                                  |
| Specific test class                    | `.\mvnw test "-Dtest=MyTestClass"`                               | `mvnw test -Dtest=MyTestClass`                                | `./mvnw test -Dtest=MyTestClass`                               |
| Specific method                        | `.\mvnw test "-Dtest=MyTestClass#myMethod"`                      | `mvnw test -Dtest=MyTestClass#myMethod`                       | `./mvnw test -Dtest=MyTestClass#myMethod`                      |
| Tests with profile (e.g., integration)  | `.\mvnw test -P integration-tests`                               | `mvnw test -P integration-tests`                              | `./mvnw test -P integration-tests`                             |
| Skip tests in any phase                | `.\mvnw <phase> -DskipTests`                                     | `mvnw <phase> -DskipTests`                                    | `./mvnw <phase> -DskipTests`                                   |

> **PowerShell Note:** `-D` parameters containing `=` must be enclosed in quotes  
> (e.g., `"-Dtest=MyClass"`) to prevent PowerShell from interpreting the `=` sign.

---

## Running with Spring Boot

| Description                         | PowerShell                                                                 | CMD                                                                    | Ubuntu / Bash                                                          |
|-------------------------------------|----------------------------------------------------------------------------|------------------------------------------------------------------------|------------------------------------------------------------------------|
| Run the application                 | `.\mvnw spring-boot:run`                                                   | `mvnw spring-boot:run`                                                 | `./mvnw spring-boot:run`                                               |
| With active profile                 | `.\mvnw spring-boot:run "-Dspring-boot.run.profiles=dev"`                  | `mvnw spring-boot:run -Dspring-boot.run.profiles=dev`                  | `./mvnw spring-boot:run -Dspring-boot.run.profiles=dev`                |
| With environment variables          | `.\mvnw spring-boot:run "-Dspring-boot.run.jvmArguments=-DVAR=value"`      | `mvnw spring-boot:run -Dspring-boot.run.jvmArguments="-DVAR=value"`    | `./mvnw spring-boot:run -Dspring-boot.run.jvmArguments="-DVAR=value"` |

---

## Installation and Dependencies

| Description                              | PowerShell                                  | CMD                                      | Ubuntu / Bash                             |
|------------------------------------------|---------------------------------------------|------------------------------------------|-------------------------------------------|
| Install artifact to `~/.m2`              | `.\mvnw install`                            | `mvnw install`                           | `./mvnw install`                          |
| Install without tests                    | `.\mvnw clean install -DskipTests`          | `mvnw clean install -DskipTests`         | `./mvnw clean install -DskipTests`        |
| Download dependencies without compiling  | `.\mvnw dependency:resolve`                 | `mvnw dependency:resolve`                | `./mvnw dependency:resolve`               |
| View dependency tree                     | `.\mvnw dependency:tree`                    | `mvnw dependency:tree`                   | `./mvnw dependency:tree`                  |
| View outdated dependencies               | `.\mvnw versions:display-dependency-updates`| `mvnw versions:display-dependency-updates`| `./mvnw versions:display-dependency-updates` |

---

## Validation and Analysis

| Description                          | PowerShell                          | CMD                               | Ubuntu / Bash                      |
|--------------------------------------|-------------------------------------|-----------------------------------|------------------------------------|
| Validate `pom.xml`                   | `.\mvnw validate`                   | `mvnw validate`                   | `./mvnw validate`                  |
| View effective POM (resolved inheritance)| `.\mvnw help:effective-pom`         | `mvnw help:effective-pom`         | `./mvnw help:effective-pom`        |
| View active profiles                 | `.\mvnw help:active-profiles`       | `mvnw help:active-profiles`       | `./mvnw help:active-profiles`      |

---

## CI/CD and Docker

```bash
# Standard build for pipelines (GitHub Actions, Docker)
./mvnw clean package -DskipTests

# With production profile
./mvnw clean package -Pprod -DskipTests

# Full verification (compile + test + package)
./mvnw verify
```

### Example: Multi-stage Dockerfile with Maven Wrapper

```dockerfile
# Stage 1: Build
FROM eclipse-temurin:17-jdk AS builder
WORKDIR /app
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw dependency:resolve
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Example: GitHub Actions

```yaml
- name: Build with Maven
  run: ./mvnw clean package -DskipTests

- name: Run tests
  run: ./mvnw verify
```

> In GitHub Actions (Linux runners), `./mvnw` is always used.

---

## Maven Lifecycle

Phases are executed in **cumulative order**. When invoking a phase, all previous ones run automatically.

```
validate → compile → test → package → verify → install → deploy
```

| Phase      | Description                                          |
|------------|------------------------------------------------------|
| `validate` | Verifies that the project is correctly configured    |
| `compile`  | Compiles the source code                             |
| `test`     | Runs unit tests                                      |
| `package`  | Packages into JAR/WAR                                |
| `verify`   | Runs additional checks (integration tests)           |
| `install`  | Installs the artifact into the local repository `~/.m2`|
| `deploy`   | Publishes the artifact to a remote repository        |

---

## Environment-Specific Notes

### PowerShell (Windows 10/11)

- Use `.\mvnw` (with a dot and backslash) to run local scripts.
- `-Dkey=value` parameters **must be enclosed in double quotes**: `"-Dtest=MyClass"`.
- If the error *"cannot be loaded because running scripts is disabled"* appears, run:
  ```powershell
  Set-ExecutionPolicy -Scope CurrentUser RemoteSigned
  ```
- Alternative if the wrapper fails: invoke directly via Java:
  ```powershell
  java -jar .mvn\wrapper\maven-wrapper.jar <phase>
  ```

### CMD (Windows 10/11)

- Use `mvnw` without a prefix (CMD searches the current directory automatically).
- `-D` parameters work without quotes in most cases.
- Separate multiple arguments with a standard space.

### Ubuntu Server 24 LTS

- Always grant execution permissions after cloning: `chmod +x mvnw`.
- Verify that Java 17 is installed:
  ```bash
  java -version
  # If not installed:
  sudo apt update && sudo apt install -y openjdk-17-jdk
  ```
- Configure `JAVA_HOME` if necessary:
  ```bash
  export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
  export PATH=$JAVA_HOME/bin:$PATH
  ```
- For background processes on servers:
  ```bash
  nohup ./mvnw spring-boot:run > app.log 2>&1 &
  ```

---

## Tips for Hexagonal Architecture with Spring Boot

```bash
# Fast build in development (no tests)
./mvnw clean package -DskipTests

# Run only integration tests with Testcontainers
./mvnw test -Dtest="*IT"

# Run only unit tests (exclude IT)
./mvnw test -Dtest="!*IT"

# View detailed test failure reports
./mvnw test -Dsurefire.useFile=false
```

---

*Generated for Java 17 + Spring Boot 3 projects with Maven Wrapper.*
o para proyectos Java 17 + Spring Boot 3 con Maven Wrapper.*
