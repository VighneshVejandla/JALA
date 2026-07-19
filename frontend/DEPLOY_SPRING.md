# Deploying the SPA inside the Spring backend (optional)

This serves the built frontend from the same JVM/origin as the API. It is
optional — the Docker/nginx setup in `README.md` needs no backend changes and
is the recommended path. Use this only if you specifically want a single
deployable artifact.

## 1. Build the SPA into the backend's static resources

```bash
# from frontend/
VITE_API_BASE_URL=/api/v1 npm run build
rm -rf ../backend/src/main/resources/static
cp -R dist ../backend/src/main/resources/static
```

(Or set `build.outDir` in `vite.config.ts` to the backend's static folder.)

## 2. Permit static assets in Spring Security

The backend currently ends its authorization rules with
`anyRequest().authenticated()`, which would block the SPA's HTML/JS/CSS. Add the
static paths to the `permitAll()` matcher in
`security/config/SecurityConfig.java`:

```java
.requestMatchers(
        "/api/v1/auth/login",
        "/swagger-ui/**",
        "/v3/api-docs/**",
        "/api-docs/**",
        "/actuator/health/**",
        "/actuator/health",
        // --- SPA static shell ---
        "/", "/index.html", "/favicon.svg", "/robots.txt",
        "/assets/**", "/manifest.webmanifest", "/sw.js", "/workbox-*.js"
).permitAll()
```

Keep the API endpoints (`/api/v1/**`) authenticated as they are — the SPA
attaches the bearer token to those calls.

## 3. Forward client-side routes to the SPA shell

React Router owns paths like `/app/ponds`. On a hard refresh Spring must return
`index.html` for those (but never for `/api/**`). Add a tiny controller:

```java
package com.jala.backend.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaForwardingController {

    // Matches non-API, non-asset paths that contain no dot (i.e. not a file).
    @GetMapping({"/", "/{path:^(?!api|assets|swagger-ui|v3|api-docs|actuator)[^.]*}",
                 "/{path:^(?!api|assets|swagger-ui|v3|api-docs|actuator)[^.]*}/**"})
    public String forward() {
        return "forward:/index.html";
    }
}
```

## 4. Rebuild and run the backend

```bash
cd ../backend
./mvnw clean package
java -jar target/*.jar
# SPA + API both served from http://localhost:8080
```
