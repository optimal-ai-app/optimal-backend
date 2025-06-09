// src/main/java/com/optimal/backend/springboot/domain/service/SummarizationClient.java
package com.optimal.backend.springboot.service;

import java.util.List;

public interface SummarizationClient {

    /**
     * Given the existing summary and a list of messages ("role: content"),
     * returns an updated conversational summary.
     */
    String updateSummary(String existingSummary, List<String> latestMessages);
}