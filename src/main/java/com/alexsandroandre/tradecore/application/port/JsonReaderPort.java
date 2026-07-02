package com.alexsandroandre.tradecore.application.port;

import com.alexsandroandre.tradecore.application.dto.RawTransactionData;
import java.io.InputStream;
import java.util.Iterator;

public interface JsonReaderPort {
    Iterator<RawTransactionData> readTransactions(InputStream inputStream);
}