package org.formentor.magnolia.ai.infrastructure.openai;

import org.formentor.magnolia.ai.domain.ImageAiService;
import org.formentor.magnolia.ai.domain.ImageFormat;
import org.formentor.magnolia.ai.domain.ImageSize;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;

public class OpenAiImageService implements ImageAiService {
    private final OpenAiApi api;

    @Inject
    public OpenAiImageService(OpenAiApiClientProvider openAiApiClientProvider) {
        api = openAiApiClientProvider.get();
    }

    @Override
    public CompletableFuture<String> generateImage(String prompt, Integer units, ImageSize size, ImageFormat format) {
        ImagesRequest request = ImagesRequest.builder()
                .prompt(prompt)
                .n(units)
                .size(size.value)
                .response_format(format.toString()) // ATTENTION: Currently the value of ImageFormat in domain matches the value in OpenAI
                .build();

        return CompletableFuture.supplyAsync(() -> api.generateImage(request))
                .thenApply(imagesGenerationResponse -> {
                    switch (format) {
                        case url:
                            return imagesGenerationResponse.getData().get(0).getUrl();
                        case base64:
                            return imagesGenerationResponse.getData().get(0).getB64_json();
                        default:
                            return null;
                    }
                });
    }
}
