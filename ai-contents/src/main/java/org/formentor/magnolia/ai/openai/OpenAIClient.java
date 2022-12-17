package org.formentor.magnolia.ai.openai;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.machinezoo.noexception.Exceptions;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.formentor.magnolia.ai.AIContentsModule;

import javax.inject.Inject;
import java.net.URL;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Multi purpose class with the following responsibilities:
 * - Client to the API of Open AI
 * - Service/domain/"use case" that provides functions to create images
 *
 * NOTE: At this moment, making 2 classes would cause over engineering.
 */
public class OpenAIClient {
    private final Optional<URL> host;
    private final OkHttpClient client;
    private final ObjectMapper objectMapper;

    @Inject
    public OpenAIClient(AIContentsModule definition, OpenAITokenProvider tokenProvider) {
        this(definition.getHost(), tokenProvider.get());
    }

    public OpenAIClient(String host, String token) {
        this.host = Exceptions.silence().get(Exceptions.wrap().supplier(() -> new URL(host)));
        this.client = new OkHttpClient.Builder().addInterceptor(chain -> {
            Request request = chain.request()
                    .newBuilder()
                    .header("Authorization", "Bearer " + token)
                    .build();
            return chain.proceed(request);
        }).build();
        this.objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public CompletableFuture<ImagesGenerationResponse> generateImageFormatUrl(String prompt, Integer units, ImagesGenerationRequest.Size size) {
        return generateImage(prompt, units, size, ImagesGenerationRequest.ResponseFormat.url);
    }

    public CompletableFuture<ImagesGenerationResponse> generateImage(String prompt, Integer units, ImagesGenerationRequest.Size size, ImagesGenerationRequest.ResponseFormat format) {
        // 'https://api.openai.com/v1/images/generations'
        URL url = host.orElseThrow(() -> new RuntimeException("Host of Open AI is not configured"));
        HttpUrl.Builder urlBuilder = new HttpUrl.Builder()
                .scheme(url.getProtocol())
                .host(url.getHost())
                .addPathSegments("v1/images/generations"); // TODO Execute just once; e.g using a high order constructor by operation: images, completion etc.
        if (url.getPort() > 0) {
            urlBuilder.port(url.getPort());
        }

        RequestBody body = buildGenerateImageRequestBody(prompt, units, size, format);
        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .post(body)
                .build();

        return CompletableFuture.supplyAsync(() -> Exceptions.wrap().get(() -> client.newCall(request).execute()))
                .thenApply(response -> {
                    if (response.code() != 200) {
                        throw new RuntimeException(String.format("Request to Open AI rejected with reason %s", response.code()));
                    }
                    return Exceptions.wrap().get(() -> response.body().string());
                } )
                .thenApply(responseBody -> Exceptions.wrap().get(() -> objectMapper.readValue(responseBody, ImagesGenerationResponse.class)));
    }

    private RequestBody buildGenerateImageRequestBody(String prompt, Integer units, ImagesGenerationRequest.Size size, ImagesGenerationRequest.ResponseFormat format) {
        ImagesGenerationRequest request = ImagesGenerationRequest.builder()
                .prompt(prompt)
                .n(units)
                .size(size.value)
                .response_format(format.toString())
                .build();

        return RequestBody.create(
                Exceptions.wrap().get(() -> objectMapper.writeValueAsString(request)), // NOTE unnecessary mapping as the String could be built directly. It adds verbosity.
                MediaType.get("application/json; charset=utf-8")
        );
    }
}
