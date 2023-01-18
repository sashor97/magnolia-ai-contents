package org.formentor.magnolia.ai.domain;

import java.util.concurrent.CompletableFuture;

public interface TextAiService {
    CompletableFuture<String> completeText(String prompt, Integer words, TextPerformance performance);
    CompletableFuture<String> editText(String prompt,TextPerformance performance,String instruction);

}
