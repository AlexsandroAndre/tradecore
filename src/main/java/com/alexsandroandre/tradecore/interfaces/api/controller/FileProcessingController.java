package com.alexsandroandre.tradecore.interfaces.api.controller;

import com.alexsandroandre.tradecore.application.service.MetricsCollector;
import com.alexsandroandre.tradecore.application.usecase.ProcessingOrchestrator;
import com.alexsandroandre.tradecore.domain.model.ProcessingMetrics;
import com.alexsandroandre.tradecore.domain.model.Transaction;
import com.alexsandroandre.tradecore.infrastructure.configuration.FileProcessingProperties;
import com.alexsandroandre.tradecore.interfaces.api.response.ProcessingResponse;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/v1/files")
public class FileProcessingController {

    private final ProcessingOrchestrator processingOrchestrator;
    private final MetricsCollector metricsCollector;
    private final ObjectMapper objectMapper;
    private final String transactionsFilePath;
    private static final int BATCH_SIZE = 10000;

    public FileProcessingController(
        ProcessingOrchestrator processingOrchestrator,
        MetricsCollector metricsCollector,
        ObjectMapper objectMapper,
        FileProcessingProperties fileProcessingProperties
    ) {
        this.processingOrchestrator = processingOrchestrator;
        this.metricsCollector = metricsCollector;
        this.objectMapper = objectMapper;
        this.transactionsFilePath = fileProcessingProperties.getTransactionsPath();
    }

    @PostMapping("/upload-transactions")
    public ResponseEntity<ProcessingResponse> uploadAndProcessTransactions(
        @RequestParam("file") MultipartFile file
    ) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        LocalDateTime startTime = LocalDateTime.now();
        long totalRecords = 0;
        long successfulRecords = 0;
        long failedRecords = 0;
        long rejectedRecords = 0;
        long duplicateRecords = 0;
        long validationErrors = 0;
        long processingErrors = 0;
        long systemErrors = 0;
        long duplicateErrors = 0;
        long batchCount = 0;
        long slowestBatchMillis = 0;

        Runtime runtime = Runtime.getRuntime();
        long peakMemoryUsageBytes = 0;
        long totalMemoryUsage = 0;
        long memoryMeasurements = 0;

        try (InputStream inputStream = file.getInputStream()) {
            JsonFactory jsonFactory = new JsonFactory();
            JsonParser jsonParser = jsonFactory.createParser(inputStream);
            jsonParser = getTransactionsArrayParser(jsonParser);

            List<Transaction> currentBatch = new ArrayList<>(BATCH_SIZE);

            while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
                if (jsonParser.currentToken() == JsonToken.START_OBJECT) {
                    JsonNode node = objectMapper.readTree(jsonParser);

                    try {
                        Transaction transaction = parseTransaction(node);
                        if (transaction != null) {
                            currentBatch.add(transaction);

                            if (currentBatch.size() >= BATCH_SIZE) {
                                long batchProcessStart = System.currentTimeMillis();
                                processBatch(currentBatch);
                                long batchProcessTime = System.currentTimeMillis() - batchProcessStart;

                                if (batchProcessTime > slowestBatchMillis) {
                                    slowestBatchMillis = batchProcessTime;
                                }

                                totalRecords += currentBatch.size();
                                successfulRecords += currentBatch.size();
                                batchCount++;
                                currentBatch.clear();
                            }
                        } else {
                            rejectedRecords++;
                            validationErrors++;
                        }
                    } catch (Exception e) {
                        failedRecords++;
                        processingErrors++;
                    }

                    long currentMemory = runtime.totalMemory() - runtime.freeMemory();
                    if (currentMemory > peakMemoryUsageBytes) {
                        peakMemoryUsageBytes = currentMemory;
                    }
                    totalMemoryUsage += currentMemory;
                    memoryMeasurements++;
                }
            }

            if (!currentBatch.isEmpty()) {
                long batchProcessStart = System.currentTimeMillis();
                processBatch(currentBatch);
                long batchProcessTime = System.currentTimeMillis() - batchProcessStart;

                if (batchProcessTime > slowestBatchMillis) {
                    slowestBatchMillis = batchProcessTime;
                }

                totalRecords += currentBatch.size();
                successfulRecords += currentBatch.size();
                batchCount++;
            }

            LocalDateTime endTime = LocalDateTime.now();
            long totalDurationMillis = java.time.temporal.ChronoUnit.MILLIS.between(startTime, endTime);

            long averageMemoryUsageBytes = memoryMeasurements > 0
                ? totalMemoryUsage / memoryMeasurements
                : 0;

            BigDecimal throughput = totalDurationMillis > 0
                ? BigDecimal.valueOf((totalRecords * 1000) / (double) totalDurationMillis)
                : BigDecimal.ZERO;

            ProcessingMetrics metrics = metricsCollector.collectMetrics(
                startTime,
                endTime,
                totalRecords,
                successfulRecords,
                failedRecords,
                duplicateRecords,
                validationErrors,
                processingErrors,
                systemErrors,
                duplicateErrors,
                BATCH_SIZE,
                batchCount,
                slowestBatchMillis,
                peakMemoryUsageBytes,
                averageMemoryUsageBytes
            );

            return ResponseEntity.ok(new ProcessingResponse(
                metrics.id(),
                (int) totalRecords,
                (int) successfulRecords,
                (int) failedRecords,
                (int) rejectedRecords,
                totalDurationMillis,
                throughput,
                (int) validationErrors,
                (int) processingErrors,
                (int) systemErrors,
                (int) duplicateErrors,
                "SUCCESS",
                "File processed successfully: " + totalRecords + " transactions processed in " + totalDurationMillis + "ms"
            ));

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ProcessingResponse(
                    UUID.randomUUID(),
                    0, 0, 0, 0, 0,
                    BigDecimal.ZERO,
                    0, 0, 0, 0,
                    "ERROR",
                    "File processing failed: " + e.getMessage()
                ));
        }
    }

    private Transaction parseTransaction(JsonNode node) {
        try {
            String transactionId = node.get("transactionId").asText();
            String accountId = node.get("accountId").asText();
            double amount = node.get("amount").asDouble();
            String currency = node.get("currency").asText();
            String timestamp = node.get("timestamp").asText();
            String source = node.get("source").asText();

            if (transactionId == null || transactionId.isEmpty() ||
                accountId == null || accountId.isEmpty() ||
                currency == null || currency.isEmpty() ||
                timestamp == null || timestamp.isEmpty() ||
                source == null || source.isEmpty()) {
                return null;
            }

            OffsetDateTime offsetDateTime = OffsetDateTime.parse(timestamp);
            
            return new Transaction(
                transactionId,
                accountId,
                BigDecimal.valueOf(amount),
                currency,
                offsetDateTime,
                source,
                Transaction.TransactionStatus.PENDING
            );
        } catch (Exception e) {
            return null;
        }
    }

    private void processBatch(List<Transaction> batch) {
        try {
            Stream<Transaction> stream = batch.stream();
            processingOrchestrator.orchestrate(stream);
        } catch (Exception e) {
        }
    }

    private JsonParser getTransactionsArrayParser(JsonParser jsonParser) throws IOException {
        while (jsonParser.nextToken() != null) {
            if (jsonParser.currentToken() == JsonToken.START_ARRAY) {
                return jsonParser;
            }
            if (jsonParser.currentToken() == JsonToken.FIELD_NAME &&
                "transactions".equals(jsonParser.currentName())) {
                jsonParser.nextToken();
                if (jsonParser.currentToken() == JsonToken.START_ARRAY) {
                    return jsonParser;
                }
            }
        }
        return jsonParser;
    }

    @GetMapping("/process-local")
    public ResponseEntity<ProcessingResponse> processLocalTransactionFile() {
        return processTransactionFile(transactionsFilePath);
    }

    public ResponseEntity<ProcessingResponse> processTransactionFile(String filePath) {
        LocalDateTime startTime = LocalDateTime.now();
        long totalRecords = 0;
        long successfulRecords = 0;
        long failedRecords = 0;
        long rejectedRecords = 0;
        long duplicateRecords = 0;
        long validationErrors = 0;
        long processingErrors = 0;
        long systemErrors = 0;
        long duplicateErrors = 0;
        long batchCount = 0;
        long slowestBatchMillis = 0;

        Runtime runtime = Runtime.getRuntime();
        long peakMemoryUsageBytes = 0;
        long totalMemoryUsage = 0;
        long memoryMeasurements = 0;

        try {
            Path resolvedPath = Paths.get(filePath);

            if (!Files.exists(resolvedPath)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ProcessingResponse(
                        UUID.randomUUID(),
                        0, 0, 0, 0, 0,
                        BigDecimal.ZERO,
                        0, 0, 0, 0,
                        "ERROR",
                        "File not found: " + filePath
                    ));
            }

            try (InputStream inputStream = Files.newInputStream(resolvedPath)) {
                JsonFactory jsonFactory = new JsonFactory();
                JsonParser jsonParser = jsonFactory.createParser(inputStream);
                jsonParser = getTransactionsArrayParser(jsonParser);

                List<Transaction> currentBatch = new ArrayList<>(BATCH_SIZE);

                while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
                    if (jsonParser.currentToken() == JsonToken.START_OBJECT) {
                        JsonNode node = objectMapper.readTree(jsonParser);

                        try {
                            Transaction transaction = parseTransaction(node);
                            if (transaction != null) {
                                currentBatch.add(transaction);

                                if (currentBatch.size() >= BATCH_SIZE) {
                                    long batchProcessStart = System.currentTimeMillis();
                                    processBatch(currentBatch);
                                    long batchProcessTime = System.currentTimeMillis() - batchProcessStart;

                                    if (batchProcessTime > slowestBatchMillis) {
                                        slowestBatchMillis = batchProcessTime;
                                    }

                                    totalRecords += currentBatch.size();
                                    successfulRecords += currentBatch.size();
                                    batchCount++;
                                    currentBatch.clear();
                                }
                            } else {
                                rejectedRecords++;
                                validationErrors++;
                            }
                        } catch (Exception e) {
                            failedRecords++;
                            processingErrors++;
                        }

                        long currentMemory = runtime.totalMemory() - runtime.freeMemory();
                        if (currentMemory > peakMemoryUsageBytes) {
                            peakMemoryUsageBytes = currentMemory;
                        }
                        totalMemoryUsage += currentMemory;
                        memoryMeasurements++;
                    }
                }

                if (!currentBatch.isEmpty()) {
                    long batchProcessStart = System.currentTimeMillis();
                    processBatch(currentBatch);
                    long batchProcessTime = System.currentTimeMillis() - batchProcessStart;

                    if (batchProcessTime > slowestBatchMillis) {
                        slowestBatchMillis = batchProcessTime;
                    }

                    totalRecords += currentBatch.size();
                    successfulRecords += currentBatch.size();
                    batchCount++;
                }

                LocalDateTime endTime = LocalDateTime.now();
                long totalDurationMillis = java.time.temporal.ChronoUnit.MILLIS.between(startTime, endTime);

                long averageMemoryUsageBytes = memoryMeasurements > 0
                    ? totalMemoryUsage / memoryMeasurements
                    : 0;

                BigDecimal throughput = totalDurationMillis > 0
                    ? BigDecimal.valueOf((totalRecords * 1000) / (double) totalDurationMillis)
                    : BigDecimal.ZERO;

                ProcessingMetrics metrics = metricsCollector.collectMetrics(
                    startTime,
                    endTime,
                    totalRecords,
                    successfulRecords,
                    failedRecords,
                    duplicateRecords,
                    validationErrors,
                    processingErrors,
                    systemErrors,
                    duplicateErrors,
                    BATCH_SIZE,
                    batchCount,
                    slowestBatchMillis,
                    peakMemoryUsageBytes,
                    averageMemoryUsageBytes
                );

                return ResponseEntity.ok(new ProcessingResponse(
                    metrics.id(),
                    (int) totalRecords,
                    (int) successfulRecords,
                    (int) failedRecords,
                    (int) rejectedRecords,
                    totalDurationMillis,
                    throughput,
                    (int) validationErrors,
                    (int) processingErrors,
                    (int) systemErrors,
                    (int) duplicateErrors,
                    "SUCCESS",
                    "File processed successfully: " + totalRecords + " transactions processed in " + totalDurationMillis + "ms"
                ));

            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ProcessingResponse(
                        UUID.randomUUID(),
                        0, 0, 0, 0, 0,
                        BigDecimal.ZERO,
                        0, 0, 0, 0,
                        "ERROR",
                        "File processing failed: " + e.getMessage()
                    ));
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ProcessingResponse(
                    UUID.randomUUID(),
                    0, 0, 0, 0, 0,
                    BigDecimal.ZERO,
                    0, 0, 0, 0,
                    "ERROR",
                    "Unexpected error: " + e.getMessage()
                ));
        }
    }
}
