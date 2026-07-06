package com.alexsandroandre.tradecore.interfaces.api.controller;

import com.alexsandroandre.tradecore.interfaces.api.request.ProcessTransactionRequest;
import com.alexsandroandre.tradecore.interfaces.api.response.ProcessingResponse;
import com.alexsandroandre.tradecore.domain.validation.DomainValidationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
class ProcessingControllerTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
        .withDatabaseName("financial_processor_test")
        .withUsername("postgres")
        .withPassword("postgres");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DomainValidationService domainValidationService;

    @BeforeEach
    void setUp() {
        domainValidationService.resetProcessedIds();
    }

    @Test
    void testProcessSingleTransaction() throws Exception {
        ProcessTransactionRequest request = new ProcessTransactionRequest(
            List.of(
                new ProcessTransactionRequest.TransactionInput(
                    "TRX001",
                    "ACC123456",
                    1500.50,
                    "USD",
                    "2025-01-01T10:30:45+00:00",
                    "MOBILE_APP"
                )
            )
        );

        MvcResult result = mockMvc.perform(
            post("/api/v1/processing/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("SUCCESS"))
            .andExpect(jsonPath("$.processingId").isNotEmpty())
            .andExpect(jsonPath("$.totalRecordsProcessed").value(1))
            .andExpect(jsonPath("$.successfulRecords").value(1))
            .andExpect(jsonPath("$.failedRecords").value(0))
            .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        ProcessingResponse response = objectMapper.readValue(responseBody, ProcessingResponse.class);

        assertNotNull(response.processingId());
        assertEquals(1, response.totalRecordsProcessed());
        assertEquals("SUCCESS", response.status());
    }

    @Test
    void testProcessMultipleTransactions() throws Exception {
        ProcessTransactionRequest request = new ProcessTransactionRequest(
            List.of(
                new ProcessTransactionRequest.TransactionInput(
                    "TRX001",
                    "ACC123456",
                    1500.50,
                    "USD",
                    "2025-01-01T10:30:45+00:00",
                    "MOBILE_APP"
                ),
                new ProcessTransactionRequest.TransactionInput(
                    "TRX002",
                    "ACC789012",
                    250.75,
                    "EUR",
                    "2025-01-01T10:31:20+00:00",
                    "WEB"
                ),
                new ProcessTransactionRequest.TransactionInput(
                    "TRX003",
                    "ACC456789",
                    5000.00,
                    "BRL",
                    "2025-01-01T10:32:00+00:00",
                    "API"
                )
            )
        );

        mockMvc.perform(
            post("/api/v1/processing/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("SUCCESS"))
            .andExpect(jsonPath("$.totalRecordsProcessed").value(3))
            .andExpect(jsonPath("$.successfulRecords").value(3))
            .andExpect(jsonPath("$.failedRecords").value(0));
    }

    @Test
    void testProcessWithValidTimestampFormats() throws Exception {
        List<String> validTimestamps = Arrays.asList(
            "2025-01-01T10:30:45+00:00",
            "2025-01-01T10:30:45Z",
            "2025-01-01T10:30:45-03:00",
            "2025-01-01T10:30:45.123+00:00"
        );

        for (String timestamp : validTimestamps) {
            ProcessTransactionRequest request = new ProcessTransactionRequest(
                List.of(
                    new ProcessTransactionRequest.TransactionInput(
                        "TRX_" + timestamp.hashCode(),
                        "ACC123456",
                        100.0,
                        "USD",
                        timestamp,
                        "WEB"
                    )
                )
            );

            mockMvc.perform(
                post("/api/v1/processing/transactions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
        }
    }

    @Test
    void testProcessWithInvalidTimestampFormat() throws Exception {
        ProcessTransactionRequest request = new ProcessTransactionRequest(
            List.of(
                new ProcessTransactionRequest.TransactionInput(
                    "TRX001",
                    "ACC123456",
                    1500.50,
                    "USD",
                    "2025-01-01 10:30:45",
                    "WEB"
                )
            )
        );

        mockMvc.perform(
            post("/api/v1/processing/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.status").value("ERROR"))
            .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void testProcessWithEmptyTransactionList() throws Exception {
        ProcessTransactionRequest request = new ProcessTransactionRequest(List.of());

        mockMvc.perform(
            post("/api/v1/processing/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest());
    }

    @Test
    void testProcessResponseStructure() throws Exception {
        ProcessTransactionRequest request = new ProcessTransactionRequest(
            List.of(
                new ProcessTransactionRequest.TransactionInput(
                    "TRX001",
                    "ACC123456",
                    1500.50,
                    "USD",
                    "2025-01-01T10:30:45+00:00",
                    "MOBILE_APP"
                )
            )
        );

        mockMvc.perform(
            post("/api/v1/processing/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.processingId").isNotEmpty())
            .andExpect(jsonPath("$.totalRecordsProcessed").isNumber())
            .andExpect(jsonPath("$.successfulRecords").isNumber())
            .andExpect(jsonPath("$.failedRecords").isNumber())
            .andExpect(jsonPath("$.duplicateRecords").isNumber())
            .andExpect(jsonPath("$.totalDurationMillis").isNumber())
            .andExpect(jsonPath("$.throughput").isNumber())
            .andExpect(jsonPath("$.validationErrors").isNumber())
            .andExpect(jsonPath("$.processingErrors").isNumber())
            .andExpect(jsonPath("$.systemErrors").isNumber())
            .andExpect(jsonPath("$.duplicateErrors").isNumber())
            .andExpect(jsonPath("$.status").value(anyOf(equalTo("SUCCESS"), equalTo("ERROR"))))
            .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void testProcessWithDifferentCurrencies() throws Exception {
        ProcessTransactionRequest request = new ProcessTransactionRequest(
            List.of(
                new ProcessTransactionRequest.TransactionInput(
                    "TRX001",
                    "ACC123456",
                    1500.50,
                    "USD",
                    "2025-01-01T10:30:45+00:00",
                    "WEB"
                ),
                new ProcessTransactionRequest.TransactionInput(
                    "TRX002",
                    "ACC789012",
                    250.75,
                    "EUR",
                    "2025-01-01T10:31:20+00:00",
                    "WEB"
                ),
                new ProcessTransactionRequest.TransactionInput(
                    "TRX003",
                    "ACC456789",
                    5000.00,
                    "BRL",
                    "2025-01-01T10:32:00+00:00",
                    "WEB"
                ),
                new ProcessTransactionRequest.TransactionInput(
                    "TRX004",
                    "ACC654321",
                    1000.00,
                    "GBP",
                    "2025-01-01T10:33:00+00:00",
                    "WEB"
                )
            )
        );

        mockMvc.perform(
            post("/api/v1/processing/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("SUCCESS"))
            .andExpect(jsonPath("$.totalRecordsProcessed").value(4));
    }

    @Test
    void testProcessWithDifferentSources() throws Exception {
        ProcessTransactionRequest request = new ProcessTransactionRequest(
            List.of(
                new ProcessTransactionRequest.TransactionInput(
                    "TRX001",
                    "ACC123456",
                    1500.50,
                    "USD",
                    "2025-01-01T10:30:45+00:00",
                    "MOBILE_APP"
                ),
                new ProcessTransactionRequest.TransactionInput(
                    "TRX002",
                    "ACC789012",
                    250.75,
                    "USD",
                    "2025-01-01T10:31:20+00:00",
                    "WEB"
                ),
                new ProcessTransactionRequest.TransactionInput(
                    "TRX003",
                    "ACC456789",
                    5000.00,
                    "USD",
                    "2025-01-01T10:32:00+00:00",
                    "ATM"
                ),
                new ProcessTransactionRequest.TransactionInput(
                    "TRX004",
                    "ACC654321",
                    1000.00,
                    "USD",
                    "2025-01-01T10:33:00+00:00",
                    "API"
                )
            )
        );

        mockMvc.perform(
            post("/api/v1/processing/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("SUCCESS"))
            .andExpect(jsonPath("$.totalRecordsProcessed").value(4));
    }

    @Test
    void testProcessWithVariousAmounts() throws Exception {
        ProcessTransactionRequest request = new ProcessTransactionRequest(
            List.of(
                new ProcessTransactionRequest.TransactionInput(
                    "TRX001",
                    "ACC123456",
                    0.01,
                    "USD",
                    "2025-01-01T10:30:45+00:00",
                    "WEB"
                ),
                new ProcessTransactionRequest.TransactionInput(
                    "TRX002",
                    "ACC789012",
                    1000000.99,
                    "USD",
                    "2025-01-01T10:31:20+00:00",
                    "WEB"
                ),
                new ProcessTransactionRequest.TransactionInput(
                    "TRX003",
                    "ACC456789",
                    50.50,
                    "USD",
                    "2025-01-01T10:32:00+00:00",
                    "WEB"
                )
            )
        );

        mockMvc.perform(
            post("/api/v1/processing/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("SUCCESS"))
            .andExpect(jsonPath("$.totalRecordsProcessed").value(3));
    }

    @Test
    void testProcessAndVerifyMetrics() throws Exception {
        ProcessTransactionRequest request = new ProcessTransactionRequest(
            List.of(
                new ProcessTransactionRequest.TransactionInput(
                    "TRX001",
                    "ACC123456",
                    1500.50,
                    "USD",
                    "2025-01-01T10:30:45+00:00",
                    "MOBILE_APP"
                )
            )
        );

        MvcResult result = mockMvc.perform(
            post("/api/v1/processing/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk())
            .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        ProcessingResponse response = objectMapper.readValue(responseBody, ProcessingResponse.class);

        assertTrue(response.totalRecordsProcessed() > 0);
        assertTrue(response.totalDurationMillis() >= 0);
        assertNotNull(response.throughput());
        assertEquals("SUCCESS", response.status());
    }
}
