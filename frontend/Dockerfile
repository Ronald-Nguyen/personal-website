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

# ---------- Stage 1b: Pokédex live demo (Angular), served at /demos/pokedex/ ----------
FROM node:20-alpine AS pokedex
# Pinned commit of github.com/Ronald-Nguyen/Pokedex-Angular; bump to update the demo.
ARG POKEDEX_REF=5a0e13d43b5cb03b4443f8f3c887f9c5c71fe46d
WORKDIR /workspace
ADD https://codeload.github.com/Ronald-Nguyen/Pokedex-Angular/tar.gz/${POKEDEX_REF} /tmp/pokedex.tar.gz
RUN tar xzf /tmp/pokedex.tar.gz --strip-components=1 && rm /tmp/pokedex.tar.gz
# The repo's lockfile is out of sync with package.json, so npm ci would fail.
RUN --mount=type=cache,target=/root/.npm \
    npm install --no-audit --no-fund --ignore-scripts
RUN NG_CLI_ANALYTICS=false npx ng build --configuration production --base-href /demos/pokedex/

# ---------- Stage 2: serve with Nginx ----------
FROM nginx:1.27-alpine
COPY frontend/nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /workspace/frontend/dist /usr/share/nginx/html
COPY --from=pokedex /workspace/dist/pokedex/browser /usr/share/nginx/html/demos/pokedex
EXPOSE 80
HEALTHCHECK --interval=15s --timeout=5s --start-period=10s --retries=3 \
  CMD wget -q -O /dev/null http://127.0.0.1/ || exit 1
