package com.alexsandroandre.tradecore.infrastructure.ingestion;

import com.alexsandroandre.tradecore.application.dto.RawTransactionData;
import com.alexsandroandre.tradecore.domain.exception.JsonReadingException;
import com.alexsandroandre.tradecore.domain.exception.MalformedRecordException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class StreamingJsonReaderTest {
    private StreamingJsonReader reader;

    @BeforeEach
    void setUp() {
        reader = new StreamingJsonReader();
    }

    @Test
    void shouldReadValidJsonFile() {
        String json = """
            [
              {
                "transactionId": "TXN001",
                "accountId": "ACC001",
                "amount": 1500.50,
                "currency": "USD",
                "timestamp": "2026-01-01T10:00:00Z",
                "source": "BANK_SYSTEM"
              },
              {
                "transactionId": "TXN002",
                "accountId": "ACC002",
                "amount": 2500.75,
                "currency": "EUR",
                "timestamp": "2026-01-01T11:00:00Z",
                "source": "BANK_SYSTEM"
              }
            ]
            """;

        InputStream input = new ByteArrayInputStream(json.getBytes());
        Iterator<RawTransactionData> iterator = reader.readTransactions(input);

        List<RawTransactionData> transactions = new ArrayList<>();
        iterator.forEachRemaining(transactions::add);

        assertEquals(2, transactions.size());

        RawTransactionData txn1 = transactions.get(0);
        assertEquals("TXN001", txn1.transactionId());
        assertEquals("ACC001", txn1.accountId());
        assertEquals(1500.50, txn1.amount());
        assertEquals("USD", txn1.currency());
        assertEquals("2026-01-01T10:00:00Z", txn1.timestamp());
        assertEquals("BANK_SYSTEM", txn1.source());

        RawTransactionData txn2 = transactions.get(1);
        assertEquals("TXN002", txn2.transactionId());
        assertEquals("ACC002", txn2.accountId());
    }

    @Test
    void shouldHandleEmptyJsonArray() {
        String json = "[]";

        InputStream input = new ByteArrayInputStream(json.getBytes());
        Iterator<RawTransactionData> iterator = reader.readTransactions(input);

        assertFalse(iterator.hasNext());
    }

    @Test
    void shouldThrowExceptionForMalformedJson() {
        String json = "[{invalid json}]";

        InputStream input = new ByteArrayInputStream(json.getBytes());
        Iterator<RawTransactionData> iterator = reader.readTransactions(input);

        assertThrows(Exception.class, iterator::hasNext);
    }

    @Test
    void shouldThrowExceptionForMissingRequiredFields() {
        String json = """
            [
              {
                "transactionId": "TXN001",
                "accountId": "ACC001"
              }
            ]
            """;

        InputStream input = new ByteArrayInputStream(json.getBytes());
        Iterator<RawTransactionData> iterator = reader.readTransactions(input);

        assertThrows(MalformedRecordException.class, iterator::hasNext);
    }

    @Test
    void shouldThrowExceptionForNonArrayRoot() {
        String json = """
            {
              "transactionId": "TXN001",
              "accountId": "ACC001"
            }
            """;

        InputStream input = new ByteArrayInputStream(json.getBytes());
        Iterator<RawTransactionData> iterator = reader.readTransactions(input);

        assertThrows(JsonReadingException.class, iterator::hasNext);
    }

    @Test
    void shouldThrowExceptionWhenNextCalledWithoutHasNext() {
        String json = "[]";

        InputStream input = new ByteArrayInputStream(json.getBytes());
        Iterator<RawTransactionData> iterator = reader.readTransactions(input);

        assertThrows(NoSuchElementException.class, iterator::next);
    }

    @Test
    void shouldSupportAdditionalFields() {
        String json = """
            [
              {
                "transactionId": "TXN001",
                "accountId": "ACC001",
                "amount": 1500.50,
                "currency": "USD",
                "timestamp": "2026-01-01T10:00:00Z",
                "source": "BANK_SYSTEM",
                "customField": "customValue",
                "nestedData": {"key": "value"}
              }
            ]
            """;

        InputStream input = new ByteArrayInputStream(json.getBytes());
        Iterator<RawTransactionData> iterator = reader.readTransactions(input);

        RawTransactionData txn = iterator.next();
        assertEquals("TXN001", txn.transactionId());
        assertFalse(txn.additionalFields().isEmpty());
    }

    @Test
    void shouldProcessLargeNumberOfRecords() {
        StringBuilder json = new StringBuilder("[");
        for (int i = 1; i <= 1000; i++) {
            if (i > 1) json.append(",");
            json.append(String.format("""
                {
                  "transactionId": "TXN%d",
                  "accountId": "ACC%d",
                  "amount": %d.50,
                  "currency": "USD",
                  "timestamp": "2026-01-01T10:00:00Z",
                  "source": "BANK_SYSTEM"
                }
                """, i, i, i * 100));
        }
        json.append("]");

        InputStream input = new ByteArrayInputStream(json.toString().getBytes());
        Iterator<RawTransactionData> iterator = reader.readTransactions(input);

        int count = 0;
        while (iterator.hasNext()) {
            RawTransactionData txn = iterator.next();
            count++;
            assertNotNull(txn.transactionId());
        }

        assertEquals(1000, count);
    }

    @Test
    void shouldProcessRecordsSequentially() {
        String json = """
            [
              {
                "transactionId": "TXN001",
                "accountId": "ACC001",
                "amount": 100.0,
                "currency": "USD",
                "timestamp": "2026-01-01T10:00:00Z",
                "source": "BANK_SYSTEM"
              },
              {
                "transactionId": "TXN002",
                "accountId": "ACC002",
                "amount": 200.0,
                "currency": "USD",
                "timestamp": "2026-01-01T11:00:00Z",
                "source": "BANK_SYSTEM"
              },
              {
                "transactionId": "TXN003",
                "accountId": "ACC003",
                "amount": 300.0,
                "currency": "USD",
                "timestamp": "2026-01-01T12:00:00Z",
                "source": "BANK_SYSTEM"
              }
            ]
            """;

        InputStream input = new ByteArrayInputStream(json.getBytes());
        Iterator<RawTransactionData> iterator = reader.readTransactions(input);

        RawTransactionData txn1 = iterator.next();
        RawTransactionData txn2 = iterator.next();
        RawTransactionData txn3 = iterator.next();

        assertEquals("TXN001", txn1.transactionId());
        assertEquals("TXN002", txn2.transactionId());
        assertEquals("TXN003", txn3.transactionId());
        assertFalse(iterator.hasNext());
    }

    @Test
    void shouldHandleInvalidFieldTypes() {
        String json = """
            [
              {
                "transactionId": "TXN001",
                "accountId": "ACC001",
                "amount": "not-a-number",
                "currency": "USD",
                "timestamp": "2026-01-01T10:00:00Z",
                "source": "BANK_SYSTEM"
              }
            ]
            """;

        InputStream input = new ByteArrayInputStream(json.getBytes());
        Iterator<RawTransactionData> iterator = reader.readTransactions(input);

        assertThrows(MalformedRecordException.class, iterator::hasNext);
    }
}