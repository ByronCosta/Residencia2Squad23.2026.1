# Residência 2 — Squad 23 | 2026.1

Sistema de gerenciamento de reservas de salas com análise de plantas baixas por visão computacional (YOLOv8) e sugestão inteligente de estações via Google Gemini.

---

## Tecnologias

| Serviço | Tecnologia | Porta |
|---|---|---|
| API de Negócio | Java 21 + Spring Boot 3.3 | 8080 |
| API de Visão Computacional | Python 3.11 + FastAPI + YOLOv8 | 8000 |
| Banco de Dados | PostgreSQL 16 | interno |

---

## Pré-requisitos

Antes de começar, instale:

- **Docker Desktop** → https://www.docker.com/products/docker-desktop/
  - Durante a instalação, marque a opção **"Use WSL 2 instead of Hyper-V"**
  - Reinicie o PC após instalar
- **Git** → https://git-scm.com/download/win
  - Durante a instalação, selecione **"Git Bash"** como terminal padrão

> ⚠️ Após instalar o Docker Desktop, abra-o e aguarde ele iniciar completamente antes de continuar.

---

## Como rodar o projeto

Abra o **Git Bash** (não o CMD nem o PowerShell) e siga os passos:

### 1. Clone o repositório

```bash
git clone --branch projeto-final --single-branch https://github.com/ByronCosta/Residencia2Squad23.2026.1.git residencia
cd residencia
```

### 2. Configure as variáveis de ambiente

```bash
cp .env.example .env
```

Abra o arquivo `.env` com o Bloco de Notas:

```bash
notepad .env
```

Preencha os campos:

```env
POSTGRES_DB=reservasacenture
POSTGRES_USER=postgres
POSTGRES_PASSWORD=SenhaForte123!
JWT_SECRET=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
GEMINI_API_KEY=sua_chave_do_gemini_aqui
```

Salve e feche o Bloco de Notas.

### 3. Suba o projeto

```bash
docker compose up --build -d
```

> ⏳ Na primeira execução, esse comando pode demorar entre 5 e 15 minutos. As próximas execuções serão muito mais rápidas.

### 4. Verifique se está tudo rodando

```bash
docker compose ps
```

Os três serviços devem aparecer com status **Up**:

```
residencia_db      Up (healthy)
residencia_java    Up
residencia_python  Up
```

---

## Documentação das APIs (Swagger)

Após subir o projeto, acesse a documentação interativa dos endpoints:

| API | URL | Descrição |
|---|---|---|
| Java API | http://localhost:8080/swagger-ui/index.html | Endpoints de negócio (salas, reservas, usuários, Gemini) |
| Python API | http://localhost:8000/docs | Endpoints de visão computacional (YOLO) |

> 💡 No Swagger da Java API, clique em **Authorize** e cole o token JWT obtido no login para testar os endpoints protegidos.

---

## Testando os endpoints

### Fluxo principal

**1. Login**
```
POST http://localhost:8080/api/accenture/auth/authenticate
Body: { "email": "seu@email.com", "password": "suasenha" }
```
Copie o token JWT retornado.

**2. Listar salas**
```
GET http://localhost:8080/salas
```

**3. Criar sala**
```
POST http://localhost:8080/salas
Body: { "endereco": "Sala 101", "lotMax": 10, "disponibilidade": true }
```

**4. Analisar planta baixa (YOLO)**
```
POST http://localhost:8000/analisar
Body: form-data → campo "file" → selecione uma imagem .jpg/.png
```

**5. Sugerir estações via Gemini**
```
POST http://localhost:8080/api/workspace/sugerir-estacoes
Header: Authorization: Bearer <token>
```

### Usando o Bruno

Importe a coleção disponível na pasta `endpoints/` do repositório.

Instale o Bruno: https://www.usebruno.com/downloads

Abra o Bruno → **Open Collection** → selecione a pasta `endpoints/`.

---

## Comandos úteis

```bash
# Ver status dos containers
docker compose ps

# Ver logs em tempo real
docker compose logs -f java-api
docker compose logs -f python-api

# Parar tudo
docker compose down

# Subir novamente (sem rebuild)
docker compose up -d

# Subir com rebuild (após atualizar o código)
docker compose up --build -d

# Resetar o banco (apaga tudo e recria com dados iniciais)
docker compose down -v
docker compose up --build -d
```

---

## Estrutura do projeto

```
residencia/
├── docker-compose.yml       # Orquestração dos containers
├── .env.example             # Modelo de variáveis de ambiente
├── db/
│   └── init.sql             # Dados iniciais do banco (carregados automaticamente)
├── java-api/                # API Spring Boot (negócio + auth + Gemini + Swagger)
│   ├── Dockerfile
│   └── src/
└── python-api/              # API FastAPI (YOLO + visão computacional)
    ├── Dockerfile
    └── main.py
```

---

## Solução de problemas

**Docker não encontrado / permission denied**
> Certifique-se de que o Docker Desktop está aberto e em execução (ícone na bandeja do sistema).

**Porta 8080 ou 8000 já em uso**
> Algum outro programa está usando essa porta. Feche-o ou reinicie o PC.

**Container Java em loop de restart**
> Verifique se o `.env` está preenchido corretamente.
```bash
docker compose logs java-api --tail 30
```

**Banco de dados vazio após subir**
> O `init.sql` só é executado quando o volume é criado pela primeira vez. Se o banco já existia:
```bash
docker compose down -v
docker compose up --build -d
```

---

## Branches do repositório

| Branch | Conteúdo |
|---|---|
| `projeto-final` | Projeto completo e funcional (use este) |
| `dev` | Código Java (Spring Boot) |
| `ia` | Código Python (FastAPI + YOLO) |
| `docker-setup` | Configurações Docker isoladas |
| `vue` | Frontend (em desenvolvimento) |
