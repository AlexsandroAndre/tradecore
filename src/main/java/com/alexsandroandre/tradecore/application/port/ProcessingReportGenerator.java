package com.alexsandroandre.tradecore.application.port;

import com.alexsandroandre.tradecore.application.dto.ProcessingReport;
import com.alexsandroandre.tradecore.domain.model.BatchProcessingResult;

public interface ProcessingReportGenerator {
    void aggregateBatchResult(BatchProcessingResult result);
    ProcessingReport generateReport();
}
