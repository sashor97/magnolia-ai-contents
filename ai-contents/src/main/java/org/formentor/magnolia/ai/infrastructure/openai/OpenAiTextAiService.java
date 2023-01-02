package org.formentor.magnolia.ai.infrastructure.openai;

import lombok.extern.slf4j.Slf4j;
import org.formentor.magnolia.ai.domain.TextAiService;
import org.formentor.magnolia.ai.domain.TextPerformance;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class OpenAiTextAiService implements TextAiService {
    private final String DEFAULT_MODEL = "text-ada-001";

    private static Map<TextPerformance, String> MODEL_MAPPING; static {
        MODEL_MAPPING = new HashMap<>();
        MODEL_MAPPING.put(TextPerformance.best, "text-davinci-003");
        MODEL_MAPPING.put(TextPerformance.high, "text-curie-001");
        MODEL_MAPPING.put(TextPerformance.medium, "text-babbage-001");
        MODEL_MAPPING.put(TextPerformance.low, "text-ada-001");
    }
    private final OpenAiApi api;

    @Inject
    public OpenAiTextAiService(OpenAiApiClientProvider openAiApiClientProvider) {
        api = openAiApiClientProvider.get();
    }

    @Override
    public CompletableFuture<String> completeText(String prompt, Integer words, TextPerformance performance) {
        CompletionRequest request = CompletionRequest.builder()
                .model(getModelForPerformance(performance))
                .prompt(prompt)
                .temperature(0.5)
                .max_tokens(words)
                .top_p(1.0)
                .frequency_penalty(0.0)
                .presence_penalty(0.0)
                .build();

        return CompletableFuture.supplyAsync(() -> api.createCompletion(request))
                .thenApply(completionResult -> completionResult.getChoices().get(0).getText());
    }

    private String getModelForPerformance(TextPerformance performance) {
        if (!MODEL_MAPPING.containsKey(performance)) {
            log.warn("Text performance \"{}\" not supported on OpenAI. Using default model \"{}\"", performance, DEFAULT_MODEL);
            return DEFAULT_MODEL;
        }
        return MODEL_MAPPING.get(performance);
    }
}
