# Harvest & Co. Organic E-Store (Microservices)

[![CI](https://github.com/dmarni/harvest-co/actions/workflows/ci.yml/badge.svg)](https://github.com/dmarni/harvest-co/actions/workflows/ci.yml)

Full-stack organic e-store built with Spring Boot 4.1 microservices, MongoDB, and React 19.

## Tech stack

- Java 21, Maven multi-module
- Spring Boot 4.1.1, Spring Cloud 2025.1.3, Gateway WebMVC
- MongoDB 8 (database-per-service)
- React 19.3, Vite 8, Tailwind CSS 4
- Stripe Checkout (test mode)

## Services

- gateway: `8080`
- identity-service: `8081`
- catalog-service: `8082`
- cart-service: `8083`
- inventory-service: `8084`
- order-service: `8085`
- payment-service: `8086`
- review-service: `8087`
- frontend: `5173`

## Local run

1. Start MongoDB

```bash
docker compose up -d mongo
```

2. Start backend services (each in a terminal)

```bash
cp .env.example .env
./mvnw -pl gateway spring-boot:run
./mvnw -pl identity-service spring-boot:run
./mvnw -pl catalog-service spring-boot:run
./mvnw -pl cart-service spring-boot:run
./mvnw -pl inventory-service spring-boot:run
./mvnw -pl order-service spring-boot:run
./mvnw -pl payment-service spring-boot:run
./mvnw -pl review-service spring-boot:run
```

3. Start frontend

```bash
cd frontend
npm install
npm run dev
```

## Key flows

- Register/login (JWT access + refresh cookie)
- Browse categories/products, add to cart
- Checkout via payment session URL
- Payment webhook marks order paid and confirms inventory
- Verified purchasers can post reviews
- Admin endpoints for catalog, inventory, orders, payments, reviews

## CI

Pull requests and pushes to `main` run GitHub Actions:

| Job | What it proves |
|---|---|
| Backend | Java 21, `./mvnw verify` across all modules |
| Frontend | `npm ci`, oxlint, TypeScript + Vite production build |
| Compose | `docker-compose.yml` is valid |
| CI | Aggregate gate — use this as the required status check |

Dependabot opens weekly PRs for Maven, npm, Actions, and Compose images. Secrets stay in `.env` (see `.env.example`); they are never committed.

## Notes

- Internal APIs are protected with `X-Internal-Key`.
- Gateway blocks `/internal/**` from public access.
- In development without Stripe keys, payment service returns a local success URL.