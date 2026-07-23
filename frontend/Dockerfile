# --- Build stage -----------------------------------------------------------
FROM node:22-alpine AS build
WORKDIR /app

# Install deps against the lockfile first for better layer caching.
COPY package.json package-lock.json ./
RUN npm ci --legacy-peer-deps

COPY . .
# Same-origin by default: nginx reverse-proxies /api to the backend, so the
# SPA calls the relative /api/v1 base. Override at build time if needed.
ARG VITE_API_BASE_URL=/api/v1
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL
RUN npm run build

# --- Runtime stage ---------------------------------------------------------
FROM nginx:1.27-alpine AS runtime

# BACKEND_URL is substituted into the nginx config at container start
# (see nginx.conf.template). Point it at your Spring backend origin.
ENV BACKEND_URL=http://backend:8080

COPY nginx.conf.template /etc/nginx/templates/default.conf.template
COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 80
