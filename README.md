# TradeCORE - Transaction Processing System

High-performance financial transaction processing system built with Spring Boot, designed for rapid processing of large-scale transaction datasets.

## Features

- **Streaming JSON Parser**: Efficient memory usage with Jackson streaming parser
- **Batch Processing**: 10,000 transactions per batch for optimal performance
- **Metrics Collection**: Comprehensive metrics tracking (throughput, memory, errors)
- **Database Persistence**: PostgreSQL integration with Hibernate ORM
- **Hexagonal Architecture**: Clean, maintainable codebase with clear separation of concerns
- **Docker Support**: Complete Docker and Docker Compose configuration
- **Type-Safe Configuration**: Spring Boot ConfigurationProperties for environment-based settings

## Quick Start

### Prerequisites

- Java 21+
- Maven 3.9+
- Docker & Docker Compose (optional)
- PostgreSQL 16+ (for local development)

### Local Development

1. **Clone the repository**:
```bash
git clone <repository-url>
cd tradecore
```

2. **Configure environment** (optional):
```bash
cp .env.example .env
# Edit .env with your settings
```

3. **Start PostgreSQL**:
```bash
docker run -d \
  --name postgres \
  -e POSTGRES_DB=financial_processor \
  -e POSTGRES_PASSWORD=postgres \
  -p 5433:5432 \
  postgres:16-alpine
```

4. **Run the application**:
```bash
./mvnw spring-boot:run
```

The API will be available at `http://localhost:8080`

### Docker Deployment

1. **Build and run with Docker Compose**:
```bash
docker-compose up --build
```

2. **Access the application**:
- API: `http://localhost:8080`
- Database: `localhost:5433`

3. **Stop the application**:
```bash
docker-compose down
```

## API Endpoints

### Process Local File
```
GET /api/v1/files/process-local
```
Processes transactions from the configured file path.

**Environment Variable**: `FILE_TRANSACTIONS_PATH` (default: `./data/transactions-20M.json`)

**Response**:
```json
{
  "metricsId": "uuid",
  "totalRecords": 20000000,
  "successfulRecords": 19999999,
  "failedRecords": 1,
  "status": "SUCCESS"
}
```

### Upload and Process
```
POST /api/v1/files/upload-transactions
Content-Type: multipart/form-data

file: <JSON file>
```
Uploads and processes transactions from a multipart file.

## Configuration

### File Processing

Configure the transaction file path via environment variable:

```bash
# Local development
export FILE_TRANSACTIONS_PATH=./data/transactions-simple.json

# Docker
docker-compose up -e FILE_TRANSACTIONS_PATH=/app/data/transactions-simple.json
```

### Database

```yaml
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5433/financial_processor
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres
```

### Java Options

```bash
JAVA_OPTS=-XX:+UseG1GC -XX:MaxRAMPercentage=75.0 -XX:+UseStringDeduplication
```

## Data Files

### Available Files

- **`data/transactions-simple.json`** (1 KB, 5 records)
  - For testing and development
  - Included in Git

- **`data/transactions-20M.json`** (2.9 GB, 20M records)
  - For production and performance testing
  - Not included in Git (too large)
  - Requires separate download or generation

### Supported JSON Formats

The application supports two JSON structures:

**Format 1: Direct Array**
```json
[
  { "transactionId": "...", ... },
  ...
]
```

**Format 2: Object with Transactions Property**
```json
{
  "transactions": [
    { "transactionId": "...", ... },
    ...
  ]
}
```

See `data/README.md` for detailed information.

## Development

### Running Tests

```bash
# All tests
./mvnw test

# Specific test class
./mvnw test -Dtest=FileProcessingControllerTest

# With coverage
./mvnw test jacoco:report
```

### Building

```bash
# Compile
./mvnw compile

# Package JAR
./mvnw package

# Skip tests
./mvnw package -DskipTests
```

### Code Style

This project follows clean code principles with:
- Hexagonal Architecture (Ports & Adapters)
- Domain-Driven Design
- Type-safe configuration
- Constructor dependency injection

See `api-context/` for architecture documentation.

## Performance

### Optimization Strategies

1. **Streaming Parser**: Low memory footprint with Jackson streaming
2. **Batch Processing**: 10,000 transactions per batch
3. **Memory Monitoring**: Tracks peak and average memory usage
4. **Throughput Metrics**: Records transactions/second
5. **Error Categorization**: Distinguishes validation, processing, and system errors

### Metrics Endpoint

```
GET /api/v1/metrics/{metricsId}
```

Returns detailed processing metrics including:
- Duration and throughput
- Success/failure rates
- Memory usage (peak, average)
- Batch processing statistics
- Error breakdown

## Architecture

```
src/
├── main/
│   ├── java/com/alexsandroandre/tradecore/
│   │   ├── domain/          # Domain models and ports
│   │   ├── application/     # Use cases and services
│   │   ├── infrastructure/  # Adapters and configuration
│   │   └── interfaces/      # API controllers and responses
│   └── resources/
│       └── application.yml  # Spring configuration
└── test/
    └── java/                # Unit and integration tests
```

See `api-context/architecture.md` for detailed documentation.

## Documentation

- **TASK-020**: File processing refactoring specification
- **DOCKER-DATA-CONFIGURATION.md**: Docker data file setup guide
- **data/README.md**: Data file formats and usage
- **api-context/**: Architecture, skills, and task documentation

## Troubleshooting

### File Not Found Error

```
Error: File not found: /app/data/transactions-20M.json
```

**Solution**: Ensure data file exists and `FILE_TRANSACTIONS_PATH` is correct. See `api-context/tasks/v1/fileProcessing/DOCKER-DATA-CONFIGURATION.md`

### Memory Issues

If the application uses excessive memory:

1. Increase JVM heap:
```bash
JAVA_OPTS=-Xmx4g docker-compose up
```

2. Or reduce batch size (modify `FileProcessingController.java`)

### Database Connection Error

```
Connection refused: localhost:5433
```

**Solution**: Ensure PostgreSQL is running:
```bash
# With Docker Compose
docker-compose up postgres

# Standalone
docker run -d -p 5433:5432 postgres:16-alpine
```

## Contributing

Please follow the coding standards and architecture guidelines defined in:
- `api-context/coding-standards.md`
- `api-context/domain-rules.md`
- `api-context/skills/backend-development.md`

## License

[Specify your license here]
