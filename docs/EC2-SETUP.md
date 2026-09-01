# Running krishna.shop on an Amazon Linux 2023 EC2 instance

A step-by-step runbook to build and run the full Docker stack on a single EC2 box.
This is a **dev/test** deployment (single host, no TLS, no external load balancer).

---

## 1. Launch the instance

| Setting | Value |
|---------|-------|
| AMI | **Amazon Linux 2023** (x86_64) |
| Instance type | **t3.large** minimum (8 GB) · **t3.xlarge** (16 GB) recommended |
| Root volume | **30 GB gp3** (default 8 GB is too small for the build) |
| Key pair | your SSH key |

> Runs ~15 containers *and* compiles 10 Java services. 4 GB (t3.medium) will OOM.

## 2. Security group (inbound)

Restrict every rule to **My IP** — this stack has no authentication on the infra UIs.

| Port | Service |
|------|---------|
| 22   | SSH |
| 8080 | API Gateway (main entry point) |
| 8761 | Eureka dashboard |
| 8025 | MailHog UI |
| 9411 | Zipkin UI |
| 8888 | Config Server (optional) |

Ports 8081–8087 do **not** need to be open — traffic flows through the gateway.

## 3. Connect

```bash
ssh -i /path/to/your-key.pem ec2-user@<EC2_PUBLIC_IP>
```

## 4. Install Docker, Compose, git

```bash
sudo dnf update -y
sudo dnf install -y docker git
sudo systemctl enable --now docker
sudo usermod -aG docker ec2-user
```

Install the Compose v2 plugin:

```bash
sudo mkdir -p /usr/local/lib/docker/cli-plugins
sudo curl -SL "https://github.com/docker/compose/releases/latest/download/docker-compose-linux-x86_64" -o /usr/local/lib/docker/cli-plugins/docker-compose
sudo chmod +x /usr/local/lib/docker/cli-plugins/docker-compose
```

Re-login so the `docker` group takes effect, then verify:

```bash
exit
# ssh back in, then:
docker version && docker compose version
```

## 5. Copy the project to the instance

From your **local** machine (not the SSH session):

```bash
scp -i /path/to/your-key.pem -r "D:/git-files/filpkart" ec2-user@<EC2_PUBLIC_IP>:~/filpkart
```

Faster for many files: zip locally, `scp` the archive, `unzip` on the instance.

## 6. Build and start

```bash
cd ~/filpkart
docker compose up -d --build
```

Watch it come up (core services first, then the business services register):

```bash
docker compose ps
docker compose logs -f config-server service-discovery
```

## 7. Smoke-test the order saga

```bash
bash scripts/smoke-test.sh
```

Expected finish: **`SUCCESS: order confirmed`**.

## 8. Open the UIs from your laptop

- Gateway API — `http://<EC2_PUBLIC_IP>:8080`
- Eureka — `http://<EC2_PUBLIC_IP>:8761`
- MailHog (confirmation emails) — `http://<EC2_PUBLIC_IP>:8025`
- Zipkin (traces) — `http://<EC2_PUBLIC_IP>:9411`

---

## Operations cheat-sheet

```bash
docker compose logs -f order-service      # tail one service
docker compose restart order-service      # restart one service
docker compose down                       # stop, keep data
docker compose down -v                    # stop + wipe DB/Kafka volumes
docker compose up -d --build              # rebuild + restart
```

## Troubleshooting

- **A service crash-loops** → `docker compose logs <service>`. Usual causes: config-server
  not reachable yet (it retries), or a Flyway schema mismatch.
- **Build killed / OOM** → instance too small; use t3.xlarge or add swap.
- **`docker: permission denied`** → you didn't re-login after `usermod -aG docker`.
- **Kafka slow to go healthy** → normal on first boot; services retry until it's up.
- **Clean slate** → `docker compose down -v` then `docker compose up -d --build`.
