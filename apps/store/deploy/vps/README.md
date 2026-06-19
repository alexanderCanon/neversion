# Store VPS Deployment

This deployment runs the Angular SPA store on a lightweight VPS without
Dokploy. GitHub Actions builds the Docker image, pushes it to GHCR, copies this
folder to the VPS, writes `.env`, and runs Docker Compose.

## VPS Requirements

Install Docker and the Docker Compose plugin on the VPS. Open inbound ports:

```text
22/tcp
80/tcp
443/tcp
```

Create a deployment directory:

```bash
sudo mkdir -p /opt/neversion/store
sudo chown "$USER":"$USER" /opt/neversion/store
```

## GitHub Secrets

Configure these repository secrets:

```text
STORE_VPS_HOST=<vps-ip-or-hostname>
STORE_VPS_USER=<ssh-user>
STORE_VPS_SSH_KEY=<private-ssh-key>
STORE_DOMAIN=<store-domain>
CADDY_EMAIL=<email-for-letsencrypt>
STORE_API_URL=https://api.example.com
STORE_SUPABASE_URL=https://project.supabase.co
STORE_SUPABASE_KEY=<supabase-anon-key>
STORE_VENDOR_UUID=<vendor-public-uuid>
```

## GitHub Packages

The workflow publishes the image to GitHub Container Registry:

```text
ghcr.io/<owner>/<repo>/store:<sha>
ghcr.io/<owner>/<repo>/store:latest
```

For private repositories, the VPS logs in to GHCR using the GitHub Actions
`GITHUB_TOKEN` during deployment. If image pulls fail after the workflow
finishes, create a PAT with `read:packages` and adjust the workflow to use it.

## Deployment

The workflow `.github/workflows/deploy-store-vps.yml` deploys from `main` when
store files, shared packages, or this deploy folder change.

Manual deployment on the VPS, after `.env` and files are present:

```bash
cd /opt/neversion/store
docker compose pull
docker compose up -d
docker compose logs -f store
```
