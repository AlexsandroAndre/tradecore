package com.alexsandroandre.tradecore.infrastructure.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "file.transactions")
public class FileProcessingProperties {

    private String path;

    public String getTransactionsPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}