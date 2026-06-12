# Setup Docker — Residência Squad 23

Este branch contém os arquivos de configuração Docker para rodar a API Java (Spring Boot), API Python (FastAPI/YOLO) e PostgreSQL juntos.

## Estrutura necessária

Antes de subir, organize as pastas assim:

projeto/

├── docker-compose.yml

├── java-docker/Dockerfile

├── python-docker/Dockerfile

├── .env              (crie a partir do .env.example)

├── java-api/         (clone do branch dev)

└── python-api/       (clone do branch ia)


## Passo a passo

```bash
# 1. Clone este branch
git clone -b docker-setup https://github.com/ByronCosta/Residencia2Squad23.2026.1.git projeto
cd projeto

# 2. Clone o código Java e Python dentro do projeto
git clone --branch dev --single-branch https://github.com/ByronCosta/Residencia2Squad23.2026.1.git java-api
git clone --branch ia  --single-branch https://github.com/ByronCosta/Residencia2Squad23.2026.1.git python-api

# 3. Crie o .env a partir do exemplo
cp .env.example .env
nano .env   # edite a senha e o JWT_SECRET

# 4. Suba tudo
docker compose up --build -d
```

## Portas

| Serviço    | Porta |
|------------|-------|
| Java API   | 8080  |
| Python API | 8000  |
| PostgreSQL | interno |

## Comandos úteis

```bash
docker compose ps              # status
docker compose logs -f java-api
docker compose logs -f python-api
docker compose down            # parar
docker compose down -v         # parar e apagar dados do banco
```

## ⚠️ Segurança

- O `private.key` e `dev-3ur3hy6il3k3anuy.pem` expostos no branch `dev` precisam ser revogados no Auth0 e removidos do histórico do git.
- Nunca commite o `.env` real (já está no `.gitignore`).
