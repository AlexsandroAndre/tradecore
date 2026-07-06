package com.alexsandroandre.tradecore.infrastructure.configuration;

import com.alexsandroandre.tradecore.infrastructure.persistence.batch.BatchInsertConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PersistenceConfiguration {

    @Bean
    public BatchInsertConfiguration batchInsertConfiguration(BatchProperties batchProperties) {
        return new BatchInsertConfiguration(
            batchProperties.getBatchSize(),
            batchProperties.isFlushAfterBatch(),
            batchProperties.isClearContextAfterFlush(),
            batchProperties.isOrderInserts()
        );
    }

    @ConfigurationProperties(prefix = "persistence.batch")
    public static class BatchProperties {
        private int batchSize = 500;
        private boolean flushAfterBatch = true;
        private boolean clearContextAfterFlush = true;
        private boolean orderInserts = true;

        public int getBatchSize() {
            return batchSize;
        }

        public void setBatchSize(int batchSize) {
            this.batchSize = batchSize;
        }

        public boolean isFlushAfterBatch() {
            return flushAfterBatch;
        }

        public void setFlushAfterBatch(boolean flushAfterBatch) {
            this.flushAfterBatch = flushAfterBatch;
        }

        public boolean isClearContextAfterFlush() {
            return clearContextAfterFlush;
        }

        public void setClearContextAfterFlush(boolean clearContextAfterFlush) {
            this.clearContextAfterFlush = clearContextAfterFlush;
        }

        public boolean isOrderInserts() {
            return orderInserts;
        }

        public void setOrderInserts(boolean orderInserts) {
            this.orderInserts = orderInserts;
        }
    }

    @Bean
    @ConfigurationProperties(prefix = "persistence.batch")
    public BatchProperties batchProperties() {
        return new BatchProperties();
    }
}
