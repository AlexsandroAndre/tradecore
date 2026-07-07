# Data Directory

This directory contains JSON files with transaction data for testing and processing.

## Files

### `transactions-simple.json` (Required)
- Small test file with 5 sample transactions
- Used for local development and unit tests
- File size: ~1 KB
- Format: JSON array with transaction objects

### `transactions-20M.json` (Optional)
- Large production data file with 20 million transactions
- Used for performance testing
- File size: ~2.9 GB
- Format: JSON object with "transactions" property containing an array
- **Not included in Git** - should be downloaded separately or generated

## JSON Format

Both files support transaction records with the following structure:

```json
{
  "transactionId": "TRX00000001",
  "accountId": "ACC100001",
  "amount": 10.10,
  "currency": "EUR",
  "timestamp": "2025-01-01T00:00:01Z",
  "source": "WEB"
}
```

### Required Fields
- `transactionId`: Unique transaction identifier (string)
- `accountId`: Account identifier (string)
- `amount`: Transaction amount (number)
- `currency`: Currency code (string, 3 letters)
- `timestamp`: ISO 8601 formatted datetime with Z timezone (string)
- `source`: Transaction source/channel (string)

## File Format Support

The application automatically detects and handles both formats:

### Format 1: Direct Array (transactions-simple.json)
```json
[
  { transaction object },
  { transaction object }
]
```

### Format 2: Object with Transactions Property (transactions-20M.json)
```json
{
  "transactions": [
    { transaction object },
    { transaction object }
  ]
}
```

## How to Use

### Local Development
```bash
cd /path/to/project
export FILE_TRANSACTIONS_PATH=./data/transactions-simple.json
./mvnw spring-boot:run
```

### Docker
```bash
docker-compose up --build
```

The application will automatically use the file specified by `FILE_TRANSACTIONS_PATH` environment variable.

## Generating Test Data

To generate additional test files:

```bash
# Using the Java application's internal generator (if implemented)
./mvnw test
```

## See Also

- `DOCKER-DATA-CONFIGURATION.md` - Detailed Docker configuration
- `../fileProcessing/TASK-020-refactor-fileprocessing.md` - File processing specification
- `FileProcessingController.java` - API implementation
