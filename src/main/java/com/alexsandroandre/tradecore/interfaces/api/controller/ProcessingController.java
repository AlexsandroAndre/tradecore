package com.alexsandroandre.tradecore.interfaces.api.controller;

import com.alexsandroandre.tradecore.application.dto.ProcessingReport;
import com.alexsandroandre.tradecore.application.service.MetricsCollector;
import com.alexsandroandre.tradecore.application.usecase.ProcessingOrchestrator;
import com.alexsandroandre.tradecore.domain.model.ProcessingMetrics;
import com.alexsandroandre.tradecore.domain.model.Transaction;
import com.alexsandroandre.tradecore.interfaces.api.request.ProcessTransactionRequest;
import com.alexsandroandre.tradecore.interfaces.api.response.ProcessingResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/v1/processing")
public class ProcessingController {

    private final ProcessingOrchestrator processingOrchestrator;
    private final MetricsCollector metricsCollector;

    public ProcessingController(
        ProcessingOrchestrator processingOrchestrator,
        MetricsCollector metricsCollector
    ) {
        this.processingOrchestrator = processingOrchestrator;
        this.metricsCollector = metricsCollector;
    }

    @PostMapping("/transactions")
    public ResponseEntity<ProcessingResponse> processTransactions(
        @RequestBody ProcessTransactionRequest request
    ) {
        if (request.transactions() == null || request.transactions().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        LocalDateTime startTime = LocalDateTime.now();

        Stream<Transaction> transactionStream = request.transactions().stream()
            .map(txInput -> {
                try {
                    OffsetDateTime timestamp = OffsetDateTime.parse(txInput.timestamp());
                    return new Transaction(
                        txInput.transactionId(),
                        txInput.accountId(),
                        BigDecimal.valueOf(txInput.amount()),
                        txInput.currency(),
                        timestamp,
                        txInput.source(),
                        Transaction.TransactionStatus.PENDING
                    );
                } catch (Exception e) {
                    throw new IllegalArgumentException("Failed to parse transaction timestamp: " + txInput.timestamp());
                }
            });

        try {
            ProcessingReport report = processingOrchestrator.orchestrate(transactionStream);
            LocalDateTime endTime = LocalDateTime.now();

            long totalDurationMillis = report.duration();
            long batchCount = Math.max(1, (long) Math.ceil(report.totalRecords() / 100.0));
            BigDecimal throughput = report.throughput() > 0
                ? BigDecimal.valueOf(report.throughput())
                : BigDecimal.ZERO;

            ProcessingMetrics metrics = metricsCollector.collectMetrics(
                startTime,
                endTime,
                report.totalRecords(),
                report.successfulRecords(),
                report.failedRecords(),
                report.rejectedRecords(),
                report.rejectedRecords(),
                0,
                0,
                0,
                100,
                batchCount,
                0,
                1024000000,
                512000000
            );

            return ResponseEntity.ok(new ProcessingResponse(
                metrics.id(),
                report.totalRecords(),
                report.successfulRecords(),
                report.failedRecords(),
                report.rejectedRecords(),
                totalDurationMillis,
                throughput,
                report.rejectedRecords(),
                0,
                0,
                0,
                "SUCCESS",
                "Processing completed successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ProcessingResponse(
                    UUID.randomUUID(),
                    0, 0, 0, 0, 0,
                    BigDecimal.ZERO,
                    0, 0, 0, 0,
                    "ERROR",
                    "Processing failed: " + e.getMessage()
                ));
        }
    }
}
