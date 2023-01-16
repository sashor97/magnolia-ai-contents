package org.formentor.magnolia.ai.infrastructure.openai;

import lombok.extern.slf4j.Slf4j;
import org.formentor.magnolia.ai.domain.TextEditAiService;
import org.formentor.magnolia.ai.domain.TextPerformance;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class OpenAiEditTextService implements TextEditAiService {
    private final String DEFAULT_MODEL = "text-davinci-edit-001";
    private final String DEFAULT_INSTRUCTION = "Fix the spelling mistakes";

    //TODO: at this moment only text-davinci-edit-001 model is available as high, in future try to integrate others types of performances
    private static final Map<TextPerformance, String> MODEL_MAPPING; static {
        MODEL_MAPPING = new HashMap<>();
        MODEL_MAPPING.put(TextPerformance.high, "text-davinci-edit-001");
    }
    private final OpenAiApi api;

    @Inject
    public OpenAiEditTextService(OpenAiApiClientProvider openAiApiClientProvider) {
        api = openAiApiClientProvider.get();
    }

    @Override
    public CompletableFuture<String> editText(String prompt,TextPerformance performance) {
        //TODO:investigate for other models now only "text-davinci-edit-001" is available
        EditRequest request = EditRequest.builder()
                .model(getModelForPerformance(performance))
                .input(prompt)
                .n(1)
                .temperature(1.0)
                .instruction(DEFAULT_INSTRUCTION)
                .top_p(1.0)
                .build();

        return CompletableFuture.supplyAsync(() -> api.createEdit(request))
                .thenApply(completionResult ->completionResult.getChoices().get(0).getText());
    }

    private String getModelForPerformance(TextPerformance performance) {
        if (!MODEL_MAPPING.containsKey(performance)) {
            log.warn("Text performance \"{}\" not supported on OpenAI. Using default model \"{}\"", performance, DEFAULT_MODEL);
            return DEFAULT_MODEL;
        }
        return MODEL_MAPPING.get(performance);
    }
}
