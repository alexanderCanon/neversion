#!/usr/bin/env bash
# migrate-imports.sh
# Migrates @neversion/models imports to @neversion/api-client in the panel.
# Run this AFTER `pnpm run api:sync` has populated packages/api-client/src/
#
# Usage: bash scripts/migrate-imports.sh

set -e

PANEL_SRC="/home/alexavers/projects/neversion/apps/panel/src"
OLD="@neversion/models"
NEW="@neversion/api-client"

echo "🔍 Checking api-client is populated..."
if [ -z "$(ls /home/alexavers/projects/neversion/packages/api-client/src/ 2>/dev/null)" ]; then
  echo "❌ packages/api-client/src/ is empty. Run 'pnpm run api:sync' first."
  exit 1
fi

echo "✅ api-client found. Migrating imports..."

# Replace all @neversion/models imports with @neversion/api-client
find "$PANEL_SRC" -name "*.ts" | while read -r file; do
  if grep -q "$OLD" "$file"; then
    sed -i "s|$OLD|$NEW|g" "$file"
    echo "  ✓ $file"
  fi
done

echo ""
echo "🎉 Done. Now run: cd apps/panel && pnpm start"
echo "   Fix any type name mismatches if the build shows errors."
