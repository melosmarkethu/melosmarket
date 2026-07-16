# MelosMarket VPS Deployment

This setup runs the full app on one VPS with Docker Compose:

- React frontend served by Nginx on port `80`
- Caddy reverse proxy on public ports `80` and `443`
- Spring Boot backend reachable through `/api`
- PostgreSQL private inside Docker
- Uploaded images stored in a Docker volume

## 1. Server prerequisites

Use Ubuntu 24.04 on the VPS. Hostinger's Docker template is fine.

Check Docker:

```bash
docker --version
docker compose version
```

## 2. Put the project on the server

Recommended location:

```bash
mkdir -p /opt/melosmarket
cd /opt/melosmarket
```

Clone the project here, or upload it with `scp`.

## 3. Create production env

```bash
cp .env.production.example .env.production
nano .env.production
```

Change at least:

- `POSTGRES_PASSWORD`
- `MELOSMARKET_JWT_SECRET`
- `PRIMARY_DOMAIN` if you deploy on a different domain first, for example `app.melosmarket.hu`

Generate a JWT secret with:

```bash
openssl rand -base64 48
```

## 4. Start the app

```bash
docker compose --env-file .env.production -f docker-compose.prod.yml up -d --build
```

Check containers:

```bash
docker compose --env-file .env.production -f docker-compose.prod.yml ps
```

Check logs:

```bash
docker compose --env-file .env.production -f docker-compose.prod.yml logs -f backend
```

## 5. Test on the VPS IP

Open:

```text
http://YOUR_VPS_IP
http://YOUR_VPS_IP/api/health
```

## 6. Point the domain

At the domain/DNS provider set:

```text
melosmarket.hu      A record -> YOUR_VPS_IP
www.melosmarket.hu  A record -> YOUR_VPS_IP
```

## 7. HTTPS

Caddy will automatically request a Let's Encrypt certificate after the domain points to the VPS and ports `80` and `443` are reachable.

Open:

```text
https://melosmarket.hu
https://melosmarket.hu/api/health
```

## Useful commands

Restart:

```bash
docker compose --env-file .env.production -f docker-compose.prod.yml restart
```

Stop:

```bash
docker compose --env-file .env.production -f docker-compose.prod.yml down
```

Update after code changes:

```bash
docker compose --env-file .env.production -f docker-compose.prod.yml up -d --build
```
