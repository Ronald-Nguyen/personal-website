# syntax=docker/dockerfile:1
# Build context: repository root (see docker-compose.yml), so that the shared
# content/profile.json is available at ../content during the build.

# ---------- Stage 1: build the static site ----------
FROM node:20-alpine AS build
WORKDIR /workspace/frontend

COPY frontend/package.json frontend/package-lock.json* ./
RUN --mount=type=cache,target=/root/.npm \
    if [ -f package-lock.json ]; then npm ci --no-audit --no-fund; else npm install --no-audit --no-fund; fi

COPY content/ /workspace/content/
COPY frontend/ ./
RUN npm run build

# ---------- Stage 2: serve with Nginx ----------
FROM nginx:1.27-alpine
COPY frontend/nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /workspace/frontend/dist /usr/share/nginx/html
EXPOSE 80
HEALTHCHECK --interval=15s --timeout=5s --start-period=10s --retries=3 \
  CMD wget -q -O /dev/null http://127.0.0.1/ || exit 1
