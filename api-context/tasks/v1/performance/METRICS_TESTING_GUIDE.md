# Testando Inputs JSON - Metrics Controller

## Endpoints Disponíveis

### 1. GET /api/v1/metrics
Retorna todas as métricas de processamento cadastradas.

**Exemplo com curl:**
```bash
curl -X GET http://localhost:8080/api/v1/metrics
```

**Resposta JSON (Array):**
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "startTime": "2025-01-01T10:00:00",
    "endTime": "2025-01-01T10:05:00",
    "totalRecordsProcessed": 1000,
    "successfulRecords": 950,
    "failedRecords": 30,
    "duplicateRecords": 20,
    "totalDurationMillis": 300000,
    "throughput": 3.33,
    "averageLatencyMillis": 300.00,
    "peakMemoryUsageBytes": 512000000,
    "averageMemoryUsageBytes": 400000000,
    "validationErrors": 10,
    "processingErrors": 15,
    "systemErrors": 5,
    "duplicateErrors": 20,
    "batchSize": 100,
    "batchCount": 10,
    "slowestBatchMillis": 35000,
    "createdAt": "2025-01-01T10:05:00"
  }
]
```

### 2. GET /api/v1/metrics/{id}
Retorna uma métrica específica pelo ID.

**Exemplo com curl:**
```bash
curl -X GET http://localhost:8080/api/v1/metrics/550e8400-e29b-41d4-a716-446655440000
```

**Resposta:**
- Status 200 (OK): Retorna o JSON da métrica
- Status 404 (Not Found): Métrica não encontrada

### 3. GET /api/v1/metrics/latest
Retorna a métrica mais recente criada.

**Exemplo com curl:**
```bash
curl -X GET http://localhost:8080/api/v1/metrics/latest
```

### 4. GET /api/v1/metrics/range
Busca métricas por intervalo de datas.

**Parâmetros de Query:**
- `start`: LocalDateTime (formato: yyyy-MM-ddTHH:mm:ss)
- `end`: LocalDateTime (formato: yyyy-MM-ddTHH:mm:ss)

**Exemplo com curl:**
```bash
curl -X GET "http://localhost:8080/api/v1/metrics/range?start=2025-01-01T09:00:00&end=2025-01-02T10:00:00"
```

## Rodando os Testes

### Executar todos os testes do MetricsController:
```bash
./mvnw test -Dtest=MetricsControllerTest
```

### Executar um teste específico:
```bash
./mvnw test -Dtest=MetricsControllerTest#testGetMetricsById
```

### Executar com saída detalhada:
```bash
./mvnw test -Dtest=MetricsControllerTest -X
```

## Testes Implementados

1. **testGetAllMetrics** - Verifica se todos as métricas são retornadas
2. **testGetMetricsById** - Busca uma métrica específica e valida todos os campos
3. **testGetMetricsById_NotFound** - Verifica o comportamento quando métrica não existe
4. **testGetLatestMetrics** - Retorna a métrica mais recente
5. **testGetMetricsByDateRange** - Busca por intervalo de datas
6. **testGetMetricsByDateRange_NoResults** - Verifica quando nenhuma métrica existe no intervalo
7. **testResponseJsonStructure** - Valida que todos os campos obrigatórios estão presentes
8. **testAllMetricsFieldsArePresent** - Valida a presença de todos os 20 campos da resposta

## Validação de Inputs JSON

Os testes verificam automaticamente:

- ✅ Estrutura correta do JSON de resposta
- ✅ Tipos de dados corretos (UUID, LocalDateTime, BigDecimal, long, int)
- ✅ Campos obrigatórios presentes
- ✅ Valores negativos não permitidos
- ✅ Recuperação correta de dados por ID
- ✅ Filtros por data range funcionando
- ✅ Status HTTP corretos (200, 404)

## Campos da Resposta ProcessingMetricsDto

| Campo | Tipo | Descrição |
|-------|------|-----------|
| id | UUID | Identificador único |
| startTime | LocalDateTime | Hora de início do processamento |
| endTime | LocalDateTime | Hora de término do processamento |
| totalRecordsProcessed | long | Total de registros processados |
| successfulRecords | long | Registros processados com sucesso |
| failedRecords | long | Registros que falharam |
| duplicateRecords | long | Registros duplicados |
| totalDurationMillis | long | Duração total em milissegundos |
| throughput | BigDecimal | Taxa de processamento (registros/segundo) |
| averageLatencyMillis | BigDecimal | Latência média em milissegundos |
| peakMemoryUsageBytes | long | Pico de memória usada (bytes) |
| averageMemoryUsageBytes | long | Memória média usada (bytes) |
| validationErrors | long | Erros de validação |
| processingErrors | long | Erros de processamento |
| systemErrors | long | Erros do sistema |
| duplicateErrors | long | Erros de duplicação |
| batchSize | int | Tamanho do batch |
| batchCount | long | Quantidade de batches |
| slowestBatchMillis | long | Tempo do batch mais lento (ms) |
| createdAt | LocalDateTime | Data de criação do registro |

## Testando com Postman

1. Importe a URL base: `http://localhost:8080/api/v1/metrics`
2. Teste cada endpoint GET
3. Valide o JSON response contra os tipos esperados
4. Teste edge cases como IDs inválidos ou ranges vazios
