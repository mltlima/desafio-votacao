# Desafio Votacao Sicredi

API REST para gerenciamento de pautas, sessoes de votacao, votos e resultado da votacao.

A aplicacao foi desenvolvida em Java 17 com Spring Boot, persistencia em PostgreSQL, migrations com Flyway e API versionada em `/api/v1`.

## Tecnologias

- Java 17
- Spring Boot 3.3.6
- Spring Web
- Spring Data JPA
- Bean Validation
- PostgreSQL
- Flyway
- H2 para testes automatizados
- JUnit 5, Mockito, Spring Boot Test e MockMvc
- springdoc-openapi / Swagger UI
- Docker Compose
- Maven Wrapper

## Pre-requisitos

- Java 17
- Docker e Docker Compose
- PowerShell no Windows

Nao e necessario ter Maven instalado localmente, pois o projeto usa Maven Wrapper (`mvnw.cmd`).

## Como Executar Localmente

Suba o PostgreSQL:

```powershell
docker compose up -d
```

Execute a aplicacao:

```powershell
.\mvnw.cmd spring-boot:run
```

A API ficara disponivel em:

```text
http://localhost:8080
```

Configuracao padrao do banco local:

```text
jdbc:postgresql://localhost:5433/votacao
user: votacao
password: votacao
```

O `docker-compose.yml` expoe o PostgreSQL em `5433:5432`.

As configuracoes tambem podem ser sobrescritas por variaveis de ambiente:

```powershell
$env:DB_URL="jdbc:postgresql://localhost:5433/votacao"
$env:DB_USERNAME="votacao"
$env:DB_PASSWORD="votacao"
```

Para parar e remover o volume do banco local:

```powershell
docker compose down -v
```

## Como Rodar Testes

Os testes nao dependem do PostgreSQL local nem do Docker Compose.

No profile `test`, a aplicacao usa H2 em modo PostgreSQL, Flyway habilitado e JPA com `ddl-auto=validate`.

```powershell
.\mvnw.cmd clean test
```

## URLs Uteis

```text
API base:      http://localhost:8080/api/v1
Swagger UI:    http://localhost:8080/swagger-ui.html
OpenAPI JSON:  http://localhost:8080/v3/api-docs
```

## Endpoints

### Criar Pauta

```http
POST /api/v1/pautas
```

Request:

```json
{
  "titulo": "Assembleia ordinaria",
  "descricao": "Discussao sobre orcamento anual"
}
```

Exemplo PowerShell:

```powershell
$pauta = Invoke-RestMethod `
  -Method Post `
  -Uri "http://localhost:8080/api/v1/pautas" `
  -ContentType "application/json" `
  -Body '{"titulo":"Assembleia ordinaria","descricao":"Discussao sobre orcamento anual"}'

$pauta
```

Response `201 Created`:

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "titulo": "Assembleia ordinaria",
  "descricao": "Discussao sobre orcamento anual",
  "createdAt": "2026-07-01T10:30:00"
}
```

### Abrir Sessao De Votacao

```http
POST /api/v1/pautas/{pautaId}/sessoes
```

Request com duracao customizada:

```json
{
  "duracaoEmMinutos": 10
}
```

Sem body ou com `{}`, a duracao default e de `1` minuto.

Exemplo PowerShell:

```powershell
$sessao = Invoke-RestMethod `
  -Method Post `
  -Uri "http://localhost:8080/api/v1/pautas/$($pauta.id)/sessoes" `
  -ContentType "application/json" `
  -Body '{"duracaoEmMinutos":10}'

$sessao
```

Response `201 Created`:

```json
{
  "id": "9a3f9ff1-47b6-44fa-9f6f-0f0f581dfc01",
  "pautaId": "550e8400-e29b-41d4-a716-446655440000",
  "openedAt": "2026-07-01T10:30:00",
  "closesAt": "2026-07-01T10:40:00",
  "createdAt": "2026-07-01T10:30:00"
}
```

### Registrar Voto

```http
POST /api/v1/pautas/{pautaId}/votos
```

Request:

```json
{
  "associadoId": "12345678901",
  "opcao": "SIM"
}
```

Exemplo PowerShell:

```powershell
$voto = Invoke-RestMethod `
  -Method Post `
  -Uri "http://localhost:8080/api/v1/pautas/$($pauta.id)/votos" `
  -ContentType "application/json" `
  -Body '{"associadoId":"12345678901","opcao":"SIM"}'

$voto
```

Response `201 Created`:

```json
{
  "id": "23f182c5-ec3f-42db-b72f-92c28069fca2",
  "pautaId": "550e8400-e29b-41d4-a716-446655440000",
  "associadoId": "12345678901",
  "opcao": "SIM",
  "createdAt": "2026-07-01T10:35:00"
}
```

Importante: no profile default, o bonus de elegibilidade fake por CPF e aleatorio. Portanto, o exemplo acima pode retornar `201 Created`, `403 Forbidden` ou `404 Not Found`, dependendo do retorno fake.

Nenhum CPF deve ser considerado sucesso garantido em runtime local. Para validar o fluxo feliz de forma deterministica, rode os testes automatizados:

```powershell
.\mvnw.cmd clean test
```

### Consultar Resultado

```http
GET /api/v1/pautas/{pautaId}/resultado
```

Exemplo PowerShell:

```powershell
$resultado = Invoke-RestMethod `
  -Method Get `
  -Uri "http://localhost:8080/api/v1/pautas/$($pauta.id)/resultado"

$resultado
```

Response `200 OK`:

```json
{
  "pautaId": "550e8400-e29b-41d4-a716-446655440000",
  "status": "ENCERRADA",
  "totalVotos": 10,
  "votosSim": 6,
  "votosNao": 4,
  "resultado": "APROVADA"
}
```

Possiveis valores de `status`:

- `NAO_INICIADA`
- `ABERTA`
- `ENCERRADA`

Possiveis valores de `resultado`:

- `APROVADA`
- `REJEITADA`
- `EMPATADA`
- `NAO_FINALIZADA`

## Regras De Negocio

- Uma pauta pode ter apenas uma sessao de votacao.
- A sessao pode receber uma duracao customizada ou usar `1` minuto por default.
- O voto so e aceito quando a sessao esta aberta: `openedAt <= now < closesAt`.
- Cada associado/CPF pode votar apenas uma vez por pauta.
- Os votos aceitos sao `SIM` e `NAO`.
- O resultado final so e calculado quando a sessao esta encerrada.
- Resultado final:
  - `APROVADA` quando `SIM > NAO`
  - `REJEITADA` quando `NAO > SIM`
  - `EMPATADA` quando `SIM == NAO`
  - `NAO_FINALIZADA` enquanto a votacao nao iniciou ou ainda esta aberta

## Erros Padronizados

Todas as respostas de erro seguem o formato:

```json
{
  "timestamp": "2026-07-01T10:30:00",
  "status": 409,
  "error": "Conflict",
  "message": "Associado ja votou nesta pauta",
  "path": "/api/v1/pautas/{pautaId}/votos"
}
```

Mapeamento principal:

- `400 Bad Request`: validacao de request, JSON invalido ou UUID invalido.
- `403 Forbidden`: CPF valido, mas associado sem permissao para votar (`UNABLE_TO_VOTE`).
- `404 Not Found`: pauta inexistente ou CPF invalido no client fake.
- `409 Conflict`: sessao inexistente/fechada, sessao duplicada ou voto duplicado.
- `500 Internal Server Error`: erro inesperado.

## Bonus CPF/Elegibilidade

Foi implementado o bonus de integracao fake de elegibilidade por CPF.

Para preservar o contrato da API, o campo `associadoId` e usado como CPF.

No runtime local/default:

```yaml
votacao:
  elegibilidade:
    random-enabled: true
```

Com `random-enabled=true`, CPFs nao mapeados usam comportamento fake aleatorio:

- CPF invalido: voto retorna `404 Not Found`.
- CPF valido com `UNABLE_TO_VOTE`: voto retorna `403 Forbidden`.
- CPF valido com `ABLE_TO_VOTE`: voto segue o fluxo normal.

Nos testes automatizados:

- `random-enabled=false`
- CPFs configurados tornam os cenarios deterministicos.
- CPFs nao mapeados retornam `ABLE_TO_VOTE` por padrao no profile `test`.

## Persistencia, Flyway E Consistencia

O schema e criado pelo Flyway em `src/main/resources/db/migration`.

Tabelas principais:

- `pautas`
- `sessoes_votacao`
- `votos`

Decisoes de consistencia:

- UUIDs sao gerados pela aplicacao.
- `spring.jpa.hibernate.ddl-auto=validate` valida o mapeamento JPA contra o schema.
- `UNIQUE (pauta_id)` impede mais de uma sessao por pauta.
- `UNIQUE (pauta_id, associado_id)` impede voto duplicado por pauta.
- `CHECK (opcao IN ('SIM', 'NAO'))` protege as opcoes de voto no banco.

## Performance

A consulta de resultado usa query agregada no banco para contar votos `SIM`, votos `NAO` e total de votos, sem carregar todos os votos em memoria.

O schema possui indice em `(pauta_id, opcao)`, apoiando a contagem por pauta e opcao.

As constraints unicas tambem funcionam como protecao final contra condicoes de corrida em cenarios concorrentes.

Ha um teste de integracao com volume maior de votos validando essa estrategia de contagem agregada. Ele insere votos diretamente via repositories para focar na consulta de resultado, sem medir o fluxo completo de registro de voto nem prometer benchmark real de producao.

## Versionamento Da API

A API e versionada por path:

```text
/api/v1
```

Mudancas futuras incompativeis devem ser publicadas em uma nova versao, por exemplo `/api/v2`, preservando clientes existentes.

## Decisoes Tecnicas

- Arquitetura simples por recurso: controller, service, repository e DTO.
- `Clock` injetado para regras temporais testaveis.
- H2 usado apenas nos testes para evitar dependencia de Docker.
- Flyway controla o schema do banco.
- `Location` e retornado em responses `201 Created`.
- `@RestControllerAdvice` padroniza erros da API.
- Swagger/OpenAPI e gerado automaticamente com springdoc.
- Logs foram adicionados nos principais fluxos de negocio.

## Possiveis Melhorias Futuras

- Autenticacao e autorizacao.
- Integracao real com servico de CPF/elegibilidade.
- Observabilidade com metricas e tracing.
- Testes de performance/carga.
- Pipeline CI.
- Versionamento `/api/v2` para mudancas incompativeis.
- Endpoints de listagem/consulta de pautas, se o produto exigir.

## Validacao Antes Da Entrega

Rodar testes:

```powershell
.\mvnw.cmd clean test
```

Validar aplicacao localmente:

```powershell
docker compose up -d
.\mvnw.cmd spring-boot:run
```
