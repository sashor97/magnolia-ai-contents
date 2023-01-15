package org.formentor.magnolia.ai.domain;

import java.util.concurrent.CompletableFuture;

public interface TextEditAiService {
    CompletableFuture<String> editText(String prompt,TextPerformance performance);

}
