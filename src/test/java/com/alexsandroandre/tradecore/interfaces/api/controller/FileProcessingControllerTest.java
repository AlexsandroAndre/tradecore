package com.alexsandroandre.tradecore.interfaces.api.controller;

import com.alexsandroandre.tradecore.domain.validation.DomainValidationService;
import com.alexsandroandre.tradecore.interfaces.api.response.ProcessingResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
class FileProcessingControllerTest {

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

    @Autowired
    private FileProcessingController fileProcessingController;

    @BeforeEach
    void setUp() {
        domainValidationService.resetProcessedIds();
    }

    @Test
    void testUploadSmallJsonFile() throws Exception {
        String jsonContent = "[" +
            "{\"transactionId\":\"TRX001\",\"accountId\":\"ACC001\",\"amount\":100.0,\"currency\":\"USD\",\"timestamp\":\"2025-01-01T10:00:00Z\",\"source\":\"WEB\"}," +
            "{\"transactionId\":\"TRX002\",\"accountId\":\"ACC002\",\"amount\":200.0,\"currency\":\"EUR\",\"timestamp\":\"2025-01-01T10:00:01Z\",\"source\":\"API\"}" +
            "]";

        MockMultipartFile file = new MockMultipartFile(
            "file",
            "transactions.json",
            "application/json",
            jsonContent.getBytes()
        );

        MvcResult result = mockMvc.perform(
            multipart("/api/v1/files/upload-transactions")
                .file(file)
        )
            .andExpect(status().isOk())
            .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        ProcessingResponse response = objectMapper.readValue(responseBody, ProcessingResponse.class);

        assertNotNull(response);
        assertTrue(response.totalRecordsProcessed() > 0);
        assertEquals("SUCCESS", response.status());
    }

    @Test
    void testUploadEmptyFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "empty.json",
            "application/json",
            "".getBytes()
        );

        mockMvc.perform(
            multipart("/api/v1/files/upload-transactions")
                .file(file)
        )
            .andExpect(status().isBadRequest());
    }

    @Test
    void testUploadLargerJsonFile() throws Exception {
        StringBuilder jsonBuilder = new StringBuilder("[");
        for (int i = 0; i < 100; i++) {
            if (i > 0) jsonBuilder.append(",");
            jsonBuilder.append(String.format(Locale.US,
                "{\"transactionId\":\"TRX%06d\",\"accountId\":\"ACC%06d\",\"amount\":%.2f,\"currency\":\"USD\",\"timestamp\":\"2025-01-01T10:00:%02dZ\",\"source\":\"WEB\"}",
                i, i, 10.0 + i, i % 60
            ));
        }
        jsonBuilder.append("]");

        MockMultipartFile file = new MockMultipartFile(
            "file",
            "transactions-large.json",
            "application/json",
            jsonBuilder.toString().getBytes()
        );

        MvcResult result = mockMvc.perform(
            multipart("/api/v1/files/upload-transactions")
                .file(file)
        )
            .andExpect(status().isOk())
            .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        ProcessingResponse response = objectMapper.readValue(responseBody, ProcessingResponse.class);

        assertNotNull(response);
        assertEquals(100, response.totalRecordsProcessed());
        assertEquals("SUCCESS", response.status());
    }

    @Test
    void testProcessLocalTransactionFileWithValidFile() throws Exception {
        String jsonContent = "[" +
            "{\"transactionId\":\"TRX000001\",\"accountId\":\"ACC000001\",\"amount\":100.50,\"currency\":\"USD\",\"timestamp\":\"2025-01-01T10:00:00Z\",\"source\":\"WEB\"}," +
            "{\"transactionId\":\"TRX000002\",\"accountId\":\"ACC000002\",\"amount\":250.75,\"currency\":\"USD\",\"timestamp\":\"2025-01-01T10:01:00Z\",\"source\":\"API\"}" +
            "]";

        Path tempFile = Files.createTempFile("transactions", ".json");
        Files.write(tempFile, jsonContent.getBytes());

        try {
            var responseEntity = fileProcessingController.processTransactionFile(tempFile.toString());
            ProcessingResponse response = responseEntity.getBody();

            assertNotNull(response);
            assertEquals(2, response.totalRecordsProcessed());
            assertEquals("SUCCESS", response.status());
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    void testProcessLocalTransactionFileWithInvalidJson() throws Exception {
        String invalidJsonContent = "{ invalid json content }";

        Path tempFile = Files.createTempFile("transactions", ".json");
        Files.write(tempFile, invalidJsonContent.getBytes());

        try {
            var responseEntity = fileProcessingController.processTransactionFile(tempFile.toString());
            ProcessingResponse response = responseEntity.getBody();

            assertNotNull(responseEntity);
            assertNotNull(response);
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    void testProcessLocalTransactionFileWithNonExistentFile() {
        String nonExistentPath = "/non/existent/path/transactions.json";

        var responseEntity = fileProcessingController.processTransactionFile(nonExistentPath);
        ProcessingResponse response = responseEntity.getBody();

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertNotNull(response);
        assertEquals("ERROR", response.status());
    }

    @Test
    void testProcessLocalTransactionFileWithEmptyFile() throws Exception {
        Path tempFile = Files.createTempFile("transactions", ".json");
        Files.write(tempFile, "[]".getBytes());

        try {
            var responseEntity = fileProcessingController.processTransactionFile(tempFile.toString());
            ProcessingResponse response = responseEntity.getBody();

            assertNotNull(response);
            assertEquals(0, response.totalRecordsProcessed());
            assertEquals("SUCCESS", response.status());
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    void testProcessLocalTransactionFileWithLargeFile() throws Exception {
        StringBuilder jsonBuilder = new StringBuilder("[");
        for (int i = 0; i < 500; i++) {
            if (i > 0) jsonBuilder.append(",");
            jsonBuilder.append(String.format(Locale.US,
                "{\"transactionId\":\"TRX%06d\",\"accountId\":\"ACC%06d\",\"amount\":%.2f,\"currency\":\"USD\",\"timestamp\":\"2025-01-01T10:00:%02dZ\",\"source\":\"FILE\"}",
                i, i, 50.0 + i, i % 60
            ));
        }
        jsonBuilder.append("]");

        Path tempFile = Files.createTempFile("transactions-large", ".json");
        Files.write(tempFile, jsonBuilder.toString().getBytes());

        try {
            var responseEntity = fileProcessingController.processTransactionFile(tempFile.toString());
            ProcessingResponse response = responseEntity.getBody();

            assertNotNull(response);
            assertEquals(500, response.totalRecordsProcessed());
            assertEquals("SUCCESS", response.status());
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }
}