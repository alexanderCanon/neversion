#!/usr/bin/env bash
set -e

# Terminal colors for readable logs
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Argument parsing
SKIP_MIGRATIONS=false
SKIP_TESTS=false
CLI_TUNNEL=""

for arg in "$@"; do
    case $arg in
        --omit-migrations|--skip-migrations)
            SKIP_MIGRATIONS=true
            ;;
        --omit-tests|--skip-tests)
            SKIP_TESTS=true
            ;;
        *)
            if [ -z "$CLI_TUNNEL" ]; then
                CLI_TUNNEL="$arg"
            fi
            ;;
    esac
done

echo -e "${BLUE}====================================================${NC}"
echo -e "${BLUE}   Neversion Backend Pipeline & Tunnel Deployment   ${NC}"
echo -e "${BLUE}====================================================${NC}"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# 0. Load .env file
if [ -f ".env" ]; then
    echo -e "${GREEN}[0/4] Loading environment variables from .env...${NC}"
    set -a
    source .env
    set +a
else
    echo -e "${YELLOW}[!] Warning: .env file not found in $SCRIPT_DIR.${NC}"
fi

# 1. Flyway migration
if [ "$SKIP_MIGRATIONS" = "true" ]; then
    echo -e "\n${YELLOW}[1/4] Step 1: Skipping Flyway migrations (--omit-migrations flag passed).${NC}"
else
    echo -e "\n${BLUE}[1/4] Step 1: Validating database migrations (Flyway)...${NC}"
    ./mvnw flyway:migrate -Dflyway.url="$SPRING_DATASOURCE_URL" -Dflyway.user="$SPRING_DATASOURCE_USERNAME" -Dflyway.password="$SPRING_DATASOURCE_PASSWORD"
    echo -e "${GREEN}✓ Flyway migrations completed successfully.${NC}"
fi

# 2. Run unit & integration test suite
if [ "$SKIP_TESTS" = "true" ]; then
    echo -e "\n${YELLOW}[2/4] Step 2: Skipping test suite (--omit-tests flag passed).${NC}"
else
    echo -e "\n${BLUE}[2/4] Step 2: Running full test suite...${NC}"
    ./mvnw test
    echo -e "${GREEN}✓ All tests passed successfully.${NC}"
fi

# 3. Build & start Docker container in PROD profile
echo -e "\n${BLUE}[3/4] Step 3: Building and launching Docker container (PROD profile)...${NC}"
docker compose -f compose.prod.yml up -d --build

echo -e "${YELLOW}Waiting for application container to become healthy on http://localhost:8080/actuator/health...${NC}"
until curl -s -f http://localhost:8080/actuator/health > /dev/null 2>&1; do
    sleep 2
    echo -n "."
done
echo -e "\n${GREEN}✓ Backend container is healthy and listening on http://localhost:8080${NC}"

# 4. Open Cloudflare Tunnel
TUNNEL_TOKEN="${CLOUDFLARED_TUNNEL_TOKEN:-}"
TUNNEL_NAME="${CLOUDFLARED_TUNNEL_ID:-${CLOUDFLARED_TUNNEL_NAME:-$CLI_TUNNEL}}"

if [ -z "$TUNNEL_TOKEN" ] && [ -z "$TUNNEL_NAME" ]; then
    echo -e "\n${YELLOW}[4/4] Step 4: Cloudflare Tunnel${NC}"
    read -p "Enter your Cloudflare Tunnel Token or Name/ID (or press Enter to skip): " TUNNEL_INPUT
    if [[ "$TUNNEL_INPUT" == ey* ]]; then
        TUNNEL_TOKEN="$TUNNEL_INPUT"
    else
        TUNNEL_NAME="$TUNNEL_INPUT"
    fi
fi

if [ -n "$TUNNEL_TOKEN" ]; then
    echo -e "\n${GREEN}[4/4] Launching Cloudflare Tunnel via Token...${NC}"
    echo -e "${BLUE}Press Ctrl+C to stop the tunnel.${NC}\n"
    exec cloudflared tunnel run --token "$TUNNEL_TOKEN"
elif [ -n "$TUNNEL_NAME" ]; then
    echo -e "\n${GREEN}[4/4] Launching Cloudflare Tunnel: ${TUNNEL_NAME}...${NC}"
    echo -e "${BLUE}Press Ctrl+C to stop the tunnel.${NC}\n"
    exec cloudflared tunnel run "$TUNNEL_NAME"
else
    echo -e "\n${YELLOW}[4/4] Cloudflare Tunnel step skipped (no tunnel token/name specified).${NC}"
    echo -e "${GREEN}Backend is running in Docker (PROD profile) on http://localhost:8080${NC}"
fi
