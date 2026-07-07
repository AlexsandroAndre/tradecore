# Docker Data Configuration for File Processing

## Overview

The file processing system supports reading transaction data from JSON files both in local development and Docker containerized environments. This document explains how to properly configure and use data files with Docker.

## File Structure Support

The application supports two JSON file structures:

### 1. Array Root Structure (Recommended)
```json
[
  {
    "transactionId": "TRX00000001",
    "accountId": "ACC100001",
    "amount": 10.10,
    "currency": "EUR",
    "timestamp": "2025-01-01T00:00:01Z",
    "source": "WEB"
  },
  ...
]
```

### 2. Object with Transactions Property
```json
{
  "transactions": [
    {
      "transactionId": "TRX00000001",
      "accountId": "ACC100001",
      "amount": 10.10,
      "currency": "EUR",
      "timestamp": "2025-01-01T00:00:01Z",
      "source": "WEB"
    },
    ...
  ]
}
```

Both formats are automatically detected and processed correctly.

## Available Data Files

### 1. `data/transactions-simple.json` (For Testing)
- **Size**: ~1 KB
- **Records**: 5 sample transactions
- **Purpose**: Local development and quick testing
- **Default**: Used when `FILE_TRANSACTIONS_PATH` is not explicitly set

### 2. `data/transactions-20M.json` (Production Data)
- **Size**: ~2.9 GB
- **Records**: 20 million transactions
- **Purpose**: Performance testing and production use
- **Structure**: Object with "transactions" property

## Configuration

### Local Development (Without Docker)

The default configuration uses `./data/transactions-20M.json`:

```yaml
file:
  transactions:
    path: ${FILE_TRANSACTIONS_PATH:./data/transactions-20M.json}
```

To use a different file, set the environment variable:

```bash
export FILE_TRANSACTIONS_PATH=./data/transactions-simple.json
./mvnw spring-boot:run
```

### Docker Container

#### Using Docker Compose (Recommended)

The `docker-compose.yml` is pre-configured to:
1. Build the image with `data/` directory included
2. Mount the local `data/` directory as a volume in the container
3. Set the `FILE_TRANSACTIONS_PATH` environment variable

**Start the application:**
```bash
docker-compose up --build
```

**Use a specific data file:**
```bash
FILE_TRANSACTIONS_PATH=/app/data/transactions-simple.json docker-compose up
```

#### Dockerfile Configuration

The Dockerfile automatically:
1. Copies the `data/` directory into the image
2. Sets proper permissions for the application user
3. Makes the data accessible at `/app/data/` inside the container

```dockerfile
COPY data data
RUN chown -R appuser:appuser /app
```

#### Environment Variable

Set `FILE_TRANSACTIONS_PATH` to control which file the application reads:

```bash
docker run \
  -e FILE_TRANSACTIONS_PATH=/app/data/transactions-simple.json \
  -p 8080:8080 \
  tradecore-app
```

## API Endpoints

### Process Local Transaction File

**Endpoint**: `GET /api/v1/files/process-local`

**Description**: Reads and processes transactions from the configured file path

**Response**: 
```json
{
  "metricsId": "uuid-string",
  "totalRecords": 5,
  "successfulRecords": 5,
  "failedRecords": 0,
  "rejectedRecords": 0,
  "totalDurationMillis": 125,
  "throughput": 40.0,
  "validationErrors": 0,
  "processingErrors": 0,
  "systemErrors": 0,
  "duplicateErrors": 0,
  "status": "SUCCESS",
  "message": "File processed successfully: 5 transactions processed in 125ms"
}
```

### Upload and Process Transactions

**Endpoint**: `POST /api/v1/files/upload-transactions`

**Content-Type**: `multipart/form-data`

**Parameters**:
- `file` (required): JSON file to process (same format requirements as above)

**Response**: Same structure as `/process-local`

## Performance Considerations

- **Memory Usage**: Transactions are processed in batches of 10,000 for optimal memory efficiency
- **Streaming Parser**: Uses Jackson streaming parser for low memory footprint
- **Peak Memory Tracking**: Application monitors and reports peak memory usage
- **Throughput**: Returns throughput metrics (transactions/second)

## Troubleshooting

### "File not found" Error

**Issue**: `File not found: /app/data/transactions-20M.json`

**Solution**:
1. Ensure the file exists in the local `data/` directory
2. Verify the Dockerfile has `COPY data data` command
3. Check the `FILE_TRANSACTIONS_PATH` environment variable is correctly set
4. For Docker, verify the volume mount: `volumes: - ./data:/app/data`

### Large File Issues

**Issue**: Application uses excessive memory with 20M file

**Solutions**:
1. Increase JVM heap: `JAVA_OPTS="-Xmx4g"`
2. Use streaming with smaller batches (adjust `BATCH_SIZE` in controller)
3. Process file in multiple chunks (implement file splitting)

### JSON Parsing Errors

**Issue**: "Invalid JSON" errors

**Solutions**:
1. Validate JSON structure with: `jq . data/transactions-20M.json`
2. Ensure files are UTF-8 encoded
3. Check for trailing commas or incomplete records
4. Verify timestamp format is ISO 8601 with Z timezone

## Production Deployment

For production:

1. **Store data in a persistent volume** (not in image):
   ```dockerfile
   VOLUME /data
   ```

2. **Use environment variables** for data paths:
   ```bash
   docker run \
     -v /persistent/data:/data \
     -e FILE_TRANSACTIONS_PATH=/data/transactions.json \
     -p 8080:8080 \
     tradecore-app
   ```

3. **Implement data validation** before processing
4. **Monitor memory** and adjust batch size if needed
5. **Enable proper logging** for debugging

## Related Files

- `FileProcessingController.java` - Main endpoint implementations
- `FileProcessingProperties.java` - Configuration property binding
- `application.yml` - Spring configuration
- `docker-compose.yml` - Docker Compose configuration
- `Dockerfile` - Docker image build configuration
