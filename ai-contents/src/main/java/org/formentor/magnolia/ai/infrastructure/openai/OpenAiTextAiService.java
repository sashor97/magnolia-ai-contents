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
    private final String DEFAULT_EDIT_TEXT_MODEL = "text-davinci-edit-001";

    private static Map<TextPerformance, String> MODEL_MAPPING;

    static {
        MODEL_MAPPING = new HashMap<>();
        MODEL_MAPPING.put(TextPerformance.best, "text-davinci-003");
        MODEL_MAPPING.put(TextPerformance.high, "text-curie-001");
        MODEL_MAPPING.put(TextPerformance.medium, "text-babbage-001");
        MODEL_MAPPING.put(TextPerformance.low, "text-ada-001");
    }

    private static Map<TextPerformance, String> MODEL_EDIT_TEXT_MAPPING;

    static {
        MODEL_EDIT_TEXT_MAPPING = new HashMap<>();
        MODEL_EDIT_TEXT_MAPPING.put(TextPerformance.best, "text-davinci-edit-001");
    }

    private final OpenAiApi api;

    @Inject
    public OpenAiTextAiService(OpenAiApiClientProvider openAiApiClientProvider) {
        api = openAiApiClientProvider.get();
    }

    @Override
    public CompletableFuture<String> completeText(String prompt, Integer words, TextPerformance performance) {
        CompletionRequest request = CompletionRequest.builder()
                .model(getModelForPerformance(performance, MODEL_MAPPING, DEFAULT_MODEL))
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

    @Override
    public CompletableFuture<String> editText(String prompt, TextPerformance performance, String instruction) {
        //TODO : investigate for other models now only "text-davinci-edit-001" is available
        EditRequest request = EditRequest.builder()
                .model(getModelForPerformance(performance, MODEL_EDIT_TEXT_MAPPING, DEFAULT_EDIT_TEXT_MODEL))
                .input(prompt)
                .n(1)
                .temperature(1.0)
                .instruction(instruction)
                .top_p(1.0)
                .build();

        return CompletableFuture.supplyAsync(() -> api.createEdit(request))
                .thenApply(completionResult -> completionResult.getChoices().get(0).getText());

    }

    private String getModelForPerformance(TextPerformance performance, Map<TextPerformance, String> modelMapping, String defaultTextModel) {
        if (!modelMapping.containsKey(performance)) {
            log.warn("Text performance \"{}\" not supported on OpenAI. Using default model \"{}\"", performance, defaultTextModel);
            return defaultTextModel;
        }
        return modelMapping.get(performance);
    }
}
