package com.alexsandroandre.tradecore.interfaces.api.controller;

import com.alexsandroandre.tradecore.application.service.MetricsCollector;
import com.alexsandroandre.tradecore.application.usecase.ProcessingOrchestrator;
import com.alexsandroandre.tradecore.domain.model.Transaction;
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

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
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
    private static final int BATCH_SIZE = 10000;

    public FileProcessingController(
        ProcessingOrchestrator processingOrchestrator,
        MetricsCollector metricsCollector,
        ObjectMapper objectMapper
    ) {
        this.processingOrchestrator = processingOrchestrator;
        this.metricsCollector = metricsCollector;
        this.objectMapper = objectMapper;
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

        try (InputStream inputStream = file.getInputStream()) {
            JsonFactory jsonFactory = new JsonFactory();
            JsonParser jsonParser = jsonFactory.createParser(inputStream);

            List<Transaction> currentBatch = new ArrayList<>(BATCH_SIZE);
            
            while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
                if (jsonParser.currentToken() == JsonToken.START_OBJECT) {
                    JsonNode node = objectMapper.readTree(jsonParser);
                    
                    try {
                        Transaction transaction = parseTransaction(node);
                        if (transaction != null) {
                            currentBatch.add(transaction);
                            
                            if (currentBatch.size() >= BATCH_SIZE) {
                                processBatch(currentBatch);
                                totalRecords += currentBatch.size();
                                successfulRecords += currentBatch.size();
                                currentBatch.clear();
                            }
                        } else {
                            rejectedRecords++;
                        }
                    } catch (Exception e) {
                        failedRecords++;
                    }
                }
            }

            if (!currentBatch.isEmpty()) {
                processBatch(currentBatch);
                totalRecords += currentBatch.size();
                successfulRecords += currentBatch.size();
            }

            LocalDateTime endTime = LocalDateTime.now();
            long totalDurationMillis = java.time.temporal.ChronoUnit.MILLIS.between(startTime, endTime);
            BigDecimal throughput = totalDurationMillis > 0 
                ? BigDecimal.valueOf((totalRecords * 1000) / totalDurationMillis)
                : BigDecimal.ZERO;

            return ResponseEntity.ok(new ProcessingResponse(
                UUID.randomUUID(),
                (int) totalRecords,
                (int) successfulRecords,
                (int) failedRecords,
                (int) rejectedRecords,
                totalDurationMillis,
                throughput,
                0,
                0,
                0,
                0,
                "SUCCESS",
                "File processed successfully: " + totalRecords + " transactions"
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
}
