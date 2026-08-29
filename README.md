# krishna.shop — E-Commerce Microservices Platform

A production-grade, Flipkart-style e-commerce platform built with **Java 17 + Spring Boot 3 + Spring Cloud**, designed to be deployed on AWS via four different strategies (EC2 manual, ASG blue/green, Tomcat WAR, and EKS/Kubernetes), with a full Jenkins CI/CD pipeline, Terraform IaC, and Prometheus/Grafana + ELK observability.

Production domain: **krishna.shop**

---

## 1. Services

| Service | Port | Type | Responsibility |
|---|---|---|---|
| `config-server`      | 8888 | Platform | Centralized externalized configuration (Spring Cloud Config) |
| `service-discovery`  | 8761 | Platform | Service registry (Netflix Eureka) |
| `api-gateway`        | 8080 | Platform | Single entry point, routing, rate-limit, JWT edge auth (Spring Cloud Gateway) |
| `user-service`       | 8081 | Business | Registration, authentication, JWT issuance, profiles, addresses |
| `product-service`    | 8082 | Business | Product catalog, categories, search, pricing |
| `cart-service`       | 8083 | Business | Shopping cart (Redis-backed), line items |
| `order-service`      | 8084 | Business | Order lifecycle, orchestration/saga, order history |
| `payment-service`    | 8085 | Business | Payment intent, mock PSP, payment status, refunds |
| `inventory-service`  | 8086 | Business | Stock levels, reservations, replenishment |
| `notification-service`| 8087 | Business | Email/SMS notifications via Kafka events |

**Standardized stack:** PostgreSQL (per-service schema), Apache Kafka (event streaming), Redis (cart + caching), JWT (stateless auth).

---

## 2. Tech Stack

- **Language/Runtime:** Java 17
- **Framework:** Spring Boot 3.2.x, Spring Cloud 2023.0.x
- **Build:** Maven (multi-module)
- **DB:** PostgreSQL 15 (Flyway migrations)
- **Messaging:** Apache Kafka
- **Cache/Session:** Redis 7
- **Auth:** Spring Security + JWT (HS256)
- **Docs:** springdoc-openapi (Swagger UI)
- **Resilience:** Resilience4j (circuit breaker, retry)
- **Observability:** Micrometer → Prometheus, Grafana, Zipkin/Sleuth (Micrometer Tracing), ELK
- **Containers:** Docker, docker-compose (local), EKS (prod)

---

## 3. Local development

```bash
# 1. Start infra (postgres, kafka, redis, etc.) + all services
docker compose up -d --build

# 2. Or run infra only, then run services from your IDE
docker compose up -d postgres kafka redis zookeeper

# 3. Build everything
mvn -T 1C clean package

# Swagger UIs are exposed per service, e.g. http://localhost:8081/swagger-ui.html
# Eureka dashboard:  http://localhost:8761
# Gateway:           http://localhost:8080
```

Startup order (docker-compose handles this via depends_on/healthchecks):
`postgres, redis, zookeeper, kafka` → `config-server` → `service-discovery` → business services → `api-gateway`.

---

## 4. Repository layout

```
krishna-shop/
├── pom.xml                      # Parent multi-module POM (dependency & plugin mgmt)
├── docker-compose.yml           # Local full-stack
├── config-repo/                 # Externalized config served by config-server
├── config-server/
├── service-discovery/
├── api-gateway/
├── common-lib/                  # Shared DTOs, error handling, JWT util, events
├── user-service/
├── product-service/
├── cart-service/
├── order-service/
├── payment-service/
├── inventory-service/
├── notification-service/
├── db/                          # (later) schema docs / seed data
├── deploy/                      # (later) ec2 / asg / tomcat / k8s
├── terraform/                   # (later) VPC, EKS, RDS, ASG, ALB, Route53
├── jenkins/                     # (later) Jenkinsfile + shared library
├── monitoring/                  # (later) prometheus, grafana, alertmanager
├── logging/                     # (later) ELK, filebeat, fluentd
└── docs/                        # architecture diagrams, runbooks, interview Q&A
```

---

## 5. Roadmap (delivery phases)

- [x] **Phase 1 — Source code** (this phase): all 9 microservices + platform services, common-lib, docker-compose, DB migrations.
- [ ] **Phase 2 — Dockerfiles & container hardening** (per-service multi-stage builds).
- [ ] **Phase 3 — Terraform IaC** (VPC, subnets, IGW, NAT, ALB, TG, EC2, ASG, RDS, ECR, EKS, IAM, CloudWatch, Route53, ACM).
- [ ] **Phase 4 — Deployment strategies**: (a) EC2 manual + systemd, (b) ASG blue/green, (c) Tomcat WAR + Nginx, (d) EKS manifests (Deployment/Service/ConfigMap/Secret/HPA/Ingress/PV/NetworkPolicy).
- [ ] **Phase 5 — CI/CD**: Jenkinsfile (checkout → build → test → SonarQube → JFrog → Docker → Trivy scan → Terraform → deploy → rollback).
- [ ] **Phase 6 — Observability & security**: Prometheus/Grafana dashboards + alerts, ELK/Filebeat/Fluentd, WAF, Secrets Manager, SSM Parameter Store, backups.
- [ ] **Phase 7 — Docs**: architecture diagrams + interview Q&A.

See `docs/architecture.md` for diagrams.
