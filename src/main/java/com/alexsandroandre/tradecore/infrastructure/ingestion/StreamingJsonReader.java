package com.alexsandroandre.tradecore.infrastructure.ingestion;

import com.alexsandroandre.tradecore.application.dto.RawTransactionData;
import com.alexsandroandre.tradecore.application.port.JsonReaderPort;
import com.alexsandroandre.tradecore.domain.exception.JsonReadingException;
import com.alexsandroandre.tradecore.domain.exception.MalformedRecordException;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

@Component
public class StreamingJsonReader implements JsonReaderPort {
    private static final Logger logger = LoggerFactory.getLogger(StreamingJsonReader.class);
    private static final JsonFactory jsonFactory = new JsonFactory();
    private static final ObjectMapper objectMapper = new ObjectMapper();
    public static final String EXPECTED_JSON_ARRAY_AT_ROOT_LEVEL = "Expected JSON array at root level";
    public static final String FAILED_TO_INITIALIZE_JSON_PARSER = "Failed to initialize JSON parser";
    public static final String ERROR_READING_JSON_STREAM = "Error reading JSON stream";
    public static final String NO_MORE_TRANSACTIONS_AVAILABLE = "No more transactions available";
    public static final String EXPECTED_JSON_OBJECT_IN_ARRAY = "Expected JSON object in array";
    public static final String FAILED_TO_PARSE_RECORD_AT_INDEX = "Failed to parse record at index ";
    public static final String TRANSACTION_ID = "transactionId";
    public static final String ACCOUNT_ID = "accountId";
    public static final String AMOUNT = "amount";
    public static final String CURRENCY = "currency";
    public static final String TIMESTAMP = "timestamp";
    public static final String SOURCE = "source";
    public static final String MISSING_REQUIRED_FIELDS_IN_TRANSACTION_RECORD = "Missing required fields in transaction record";

    @Override
    public Iterator<RawTransactionData> readTransactions(InputStream inputStream) {
        return new TransactionIterator(inputStream);
    }

    private static class TransactionIterator implements Iterator<RawTransactionData>, AutoCloseable {
        private JsonParser parser;
        private boolean hasNextCached = false;
        private boolean hasComputedNext = false;
        private RawTransactionData nextTransaction = null;
        private int recordIndex = 0;
        private boolean closed = false;
        private IOException initializationError = null;

        TransactionIterator(InputStream inputStream) {
            try {
                this.parser = jsonFactory.createParser(inputStream);
                logger.debug("JSON file processing started");

                JsonToken token = parser.nextToken();
                if (token != JsonToken.START_ARRAY) {
                    this.initializationError = new IOException(EXPECTED_JSON_ARRAY_AT_ROOT_LEVEL);
                }
            } catch (IOException e) {
                this.initializationError = e;
            }
        }

        @Override
        public boolean hasNext() {
            if (hasComputedNext) {
                return hasNextCached;
            }

            if (initializationError != null) {
                throw new JsonReadingException(FAILED_TO_INITIALIZE_JSON_PARSER, initializationError);
            }

            try {
                hasNextCached = computeNext();
                hasComputedNext = true;
                return hasNextCached;
            } catch (IOException e) {
                close();
                throw new JsonReadingException(ERROR_READING_JSON_STREAM, e);
            }
        }

        @Override
        public RawTransactionData next() {
            if (!hasNext()) {
                throw new NoSuchElementException(NO_MORE_TRANSACTIONS_AVAILABLE);
            }

            hasComputedNext = false;
            return nextTransaction;
        }

        private boolean computeNext() throws IOException {
            if (closed) {
                return false;
            }

            JsonToken token = parser.nextToken();

            if (token == JsonToken.END_ARRAY) {
                close();
                logger.debug("JSON parsing completed. Total records processed: {}", recordIndex);
                return false;
            }

            if (token != JsonToken.START_OBJECT) {
                throw new JsonReadingException(EXPECTED_JSON_OBJECT_IN_ARRAY);
            }

            try {
                nextTransaction = parseTransaction();
                recordIndex++;
                return true;
            } catch (MalformedRecordException e) {
                throw e;
            } catch (Exception e) {
                throw new MalformedRecordException(
                    FAILED_TO_PARSE_RECORD_AT_INDEX + recordIndex,
                    recordIndex,
                    e
                );
            }
        }

        private RawTransactionData parseTransaction() throws IOException {
            Map<String, Object> additionalFields = new HashMap<>();
            String transactionId = null;
            String accountId = null;
            Double amount = null;
            String currency = null;
            String timestamp = null;
            String source = null;

            while (parser.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = parser.currentName();
                parser.nextToken();

                switch (fieldName) {
                    case TRANSACTION_ID -> transactionId = parser.getText();
                    case ACCOUNT_ID -> accountId = parser.getText();
                    case AMOUNT -> amount = parser.getDoubleValue();
                    case CURRENCY -> currency = parser.getText();
                    case TIMESTAMP -> timestamp = parser.getText();
                    case SOURCE -> source = parser.getText();
                    default -> {
                        JsonNode node = objectMapper.readTree(parser);
                        if (node.isNumber()) {
                            additionalFields.put(fieldName, node.numberValue());
                        } else if (node.isBoolean()) {
                            additionalFields.put(fieldName, node.booleanValue());
                        } else if (node.isTextual()) {
                            additionalFields.put(fieldName, node.textValue());
                        } else {
                            additionalFields.put(fieldName, objectMapper.convertValue(node, Object.class));
                        }
                    }
                }
            }

            if (transactionId == null || accountId == null || amount == null ||
                currency == null || timestamp == null || source == null) {
                throw new MalformedRecordException(
                        MISSING_REQUIRED_FIELDS_IN_TRANSACTION_RECORD,
                    recordIndex
                );
            }

            return new RawTransactionData(
                transactionId,
                accountId,
                amount,
                currency,
                timestamp,
                source,
                additionalFields
            );
        }

        @Override
        public void close() {
            if (!closed) {
                try {
                    if (parser != null) {
                        parser.close();
                    }
                    closed = true;
                } catch (IOException e) {
                    logger.error("Error closing JSON parser", e);
                }
            }
        }
    }
}