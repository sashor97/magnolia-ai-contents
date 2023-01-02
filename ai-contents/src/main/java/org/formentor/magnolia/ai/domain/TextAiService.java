package org.formentor.magnolia.ai.domain;

import java.util.concurrent.CompletableFuture;

public interface TextAiService {
    CompletableFuture<String> completeText(String prompt, Integer words, TextPerformance performance);
}
