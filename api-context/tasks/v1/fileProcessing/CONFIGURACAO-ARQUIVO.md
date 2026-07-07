# Configuração e Uso - TASK-020 File Processing

## 🔧 Configuração

### Propriedades de Arquivo

O sistema agora suporta configuração centralizada de caminhos de arquivo via `FileProcessingProperties`.

#### application.yml (Padrão)

```yaml
file:
  transactions:
    path: ${FILE_TRANSACTIONS_PATH:./data/transactions-20M.json}
```

**Explicação**:
- `file.transactions.path`: Propriedade raiz
- `${FILE_TRANSACTIONS_PATH:...}`: Variável de ambiente com fallback padrão
- `./data/transactions-20M.json`: Caminho padrão relativo (criado no diretório raiz)

---

## 🚀 Como Usar

### 1. **Desenvolvimento Local**

#### Setup Inicial

```bash
# Criar diretório de dados
mkdir -p data

# Criar arquivo de transações de exemplo
# Veja seção "Exemplo de JSON" abaixo
```

#### Executar Aplicação

```bash
# Caminho padrão
./mvnw spring-boot:run

# Com path customizado
export FILE_TRANSACTIONS_PATH=/path/to/transactions-20M.json
./mvnw spring-boot:run
```

#### Testar Endpoint

```bash
# Processar arquivo padrão
curl http://localhost:8080/api/v1/files/process-local

# Resposta esperada (sucesso)
{
  "processingId": "uuid-aqui",
  "totalRecordsProcessed": 20000000,
  "successfulRecords": 19995000,
  "failedRecords": 5000,
  "rejectedRecords": 0,
  "totalDurationMillis": 45000,
  "throughput": 444444.44,
  ...
}

# Resposta esperada (arquivo não encontrado)
{
  "processingId": "uuid-aqui",
  "totalRecordsProcessed": 0,
  "status": "ERROR",
  "message": "File not found: ./data/transactions-20M.json"
}
```

---

### 2. **Testes Unitários**

Os testes usam `processTransactionFile(String filePath)` para passar arquivos temporários:

```java
@Test
void testWithCustomFile() throws Exception {
    String jsonContent = "[{...}]";
    Path tempFile = Files.createTempFile("transactions", ".json");
    Files.write(tempFile, jsonContent.getBytes());
    
    try {
        var response = fileProcessingController.processTransactionFile(tempFile.toString());
        assertNotNull(response.getBody());
    } finally {
        Files.deleteIfExists(tempFile);
    }
}
```

**Nota**: Os testes **não dependem** do arquivo configurado, eles criam arquivos temporários.

---

### 3. **Produção**

#### Configurar Variável de Ambiente

```bash
# Linux/macOS
export FILE_TRANSACTIONS_PATH=/mnt/data/transactions-20M.json

# Docker
docker run \
  -e FILE_TRANSACTIONS_PATH=/data/transactions-20M.json \
  -v /local/data:/data \
  tradecore:latest

# Kubernetes
env:
  - name: FILE_TRANSACTIONS_PATH
    value: /mnt/shared/transactions-20M.json
```

#### Executar

```bash
java -jar tradecore.jar
```

---

## 📋 Exemplo de JSON

### Arquivo: data/transactions-20M.json

```json
[
  {
    "transactionId": "TRX000001",
    "accountId": "ACC000001",
    "amount": 1234.56,
    "currency": "USD",
    "timestamp": "2025-01-01T10:00:00Z",
    "source": "WEB"
  },
  {
    "transactionId": "TRX000002",
    "accountId": "ACC000002",
    "amount": 5678.90,
    "currency": "EUR",
    "timestamp": "2025-01-01T10:00:01Z",
    "source": "API"
  },
  ...
  20 MILHÕES DE TRANSAÇÕES
]
```

---

## 🏗️ Arquitetura de Configuração

### FileProcessingProperties

```java
@Component
@ConfigurationProperties(prefix = "file.transactions")
public class FileProcessingProperties {
    private String path;
    
    public String getTransactionsPath() { return path; }
    public void setPath(String path) { this.path = path; }
}
```

**Vantagens**:
- ✅ Type-safe property binding
- ✅ IDE autocomplete support
- ✅ Validation support
- ✅ Constructor injection (melhor que @Value)
- ✅ Testável

### FileProcessingController

```java
public FileProcessingController(
    ProcessingOrchestrator processingOrchestrator,
    MetricsCollector metricsCollector,
    ObjectMapper objectMapper,
    FileProcessingProperties fileProcessingProperties  // ← Injeção
) {
    this.transactionsFilePath = fileProcessingProperties.getTransactionsPath();
}
```

**Benefícios**:
- ✅ Dependências explícitas
- ✅ Fácil mockar em testes
- ✅ Segue Clean Code principles
- ✅ Não usa @Value (campo mutável)

---

## 🐛 Resolvendo Problemas

### Erro: "File not found"

```
ERROR: File not found: ./data/transactions-20M.json
```

**Solução**:
1. Criar diretório: `mkdir -p data`
2. Colocar arquivo: `cp /path/to/transactions-20M.json data/`
3. Verificar permissões: `ls -la data/`

### Erro: "Permission denied"

```
ERROR: File processing failed: Permission denied
```

**Solução**:
```bash
chmod 644 data/transactions-20M.json
chmod 755 data/
```

### Erro: "Out of memory"

```
ERROR: Java heap space
```

**Solução**:
```bash
# Aumentar memória heap
export JAVA_OPTS="-Xmx4g"
java -jar tradecore.jar

# Ou em Docker
docker run -e JAVA_OPTS="-Xmx4g" tradecore:latest
```

### Arquivo não é encontrado no Docker

**Solução**:
```bash
# Montar volume
docker run -v /local/data:/container/data \
  -e FILE_TRANSACTIONS_PATH=/container/data/transactions-20M.json \
  tradecore:latest
```

---

## 📊 Configurações por Ambiente

### application.yml (Padrão - Desenvolvimento)

```yaml
file:
  transactions:
    path: ./data/transactions-20M.json
```

### application-dev.yml

```yaml
file:
  transactions:
    path: ./data/transactions-dev.json
```

### application-test.yml

```yaml
file:
  transactions:
    path: ./data/transactions-test.json
```

### application-prod.yml

```yaml
file:
  transactions:
    path: /mnt/production/data/transactions-20M.json
```

**Usar**:
```bash
./mvnw spring-boot:run -Dspring.profiles.active=dev
./mvnw test -Dspring.profiles.active=test
java -Dspring.profiles.active=prod -jar tradecore.jar
```

---

## ✅ Verificação

### Verificar Configuração Ativa

```bash
# Adicionar log à aplicação
curl http://localhost:8080/actuator/env | grep file.transactions.path

# Ou via Java
FileProcessingProperties props = context.getBean(FileProcessingProperties.class);
System.out.println("Usando: " + props.getTransactionsPath());
```

### Tester com Arquivo Temporário

```java
// Via teste
Path tempFile = Files.createTempFile("test", ".json");
ResponseEntity<ProcessingResponse> response = 
    controller.processTransactionFile(tempFile.toString());
```

---

## 🎯 Migração de Código

### Antes (com @Value)

```java
@Value("${file.transactions.path:transactions-20M.json}")
private String transactionsFilePath;
```

**Problemas**:
- ❌ Campo mutável
- ❌ Difícil de mockar
- ❌ Nenhuma validação
- ❌ Acoplamento ao Spring

### Depois (com Constructor Injection)

```java
private final String transactionsFilePath;

public FileProcessingController(
    ...,
    FileProcessingProperties fileProcessingProperties
) {
    this.transactionsFilePath = fileProcessingProperties.getTransactionsPath();
}
```

**Vantagens**:
- ✅ Campo imutável (final)
- ✅ Fácil mockar
- ✅ Type-safe
- ✅ Clean Code
- ✅ Testável

---

## 📝 Checklist

- [ ] Criar diretório: `mkdir -p data`
- [ ] Colocar arquivo: `transactions-20M.json` em `./data/`
- [ ] Executar: `./mvnw spring-boot:run`
- [ ] Testar: `curl http://localhost:8080/api/v1/files/process-local`
- [ ] Verificar response: status 200 com `"SUCCESS"`
- [ ] Rodar testes: `./mvnw test`
- [ ] Configurar variável de ambiente (produção)

---

## 🔗 Referências

- TASK-020: Refactor FileProcessingController
- FileProcessingController.java
- FileProcessingProperties.java
- application.yml
- backend-development.md