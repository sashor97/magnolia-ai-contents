package org.formentor.ai.infrastructure.openai;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.apache.logging.log4j.util.Strings;
import org.formentor.magnolia.ai.AIContentsModule;
import org.formentor.magnolia.ai.domain.ImageFormat;
import org.formentor.magnolia.ai.domain.ImageSize;
import org.formentor.magnolia.ai.infrastructure.openai.OpenAiApiClientProvider;
import org.formentor.magnolia.ai.infrastructure.openai.OpenAiImageService;
import org.formentor.magnolia.ai.infrastructure.openai.TokenProvider;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OpenAiImageServiceTest {

    @Test
    public void generateImage_should_post_request_to_OpenAI() throws IOException, InterruptedException {
        final String prompt = "A cute baby sea otter";
        MockWebServer mockOpenAIServer = mockupWebServer("does-not-mind");

        final String openAIHost = mockOpenAIServer.url(Strings.EMPTY).toString();
        final String token = "sk-...DeVF";
        OpenAiImageService imageService = new OpenAiImageService(new OpenAiApiClientProvider(mockAIContentsModule(openAIHost), mockTokenProvider(token)));

        imageService.generateImage(prompt, 1, ImageSize.Size256, ImageFormat.url).join();

        RecordedRequest request = mockOpenAIServer.takeRequest();
        assertEquals("POST", request.getMethod());
        assertEquals("/v1/images/generations", request.getPath());
        assertEquals("{\"prompt\":\"A cute baby sea otter\",\"n\":1,\"size\":\"256x256\",\"response_format\":\"url\"}", request.getBody().readUtf8());
        assertEquals(String.format("Bearer %s", token), request.getHeader("Authorization"));

        mockOpenAIServer.shutdown();
    }

    @Test
    public void generateImage_should_return_url_of_the_image() throws ExecutionException, InterruptedException, IOException {
        final String prompt = "A cute baby sea otter";
        final String imageUrl = "https://oaidalleapiprodscus.blob.core.windows.net/private/org-x/user-j/img-Io.png?st=2022-12-14T18%3A19%3A43Z&skv=2021-08-06&sig=A%3D";
        MockWebServer mockOpenAIServer = mockupWebServer(imageUrl);

        final String openAIHost = mockOpenAIServer.url(Strings.EMPTY).toString();
        final String token = "sk-...DeVF";
        OpenAiImageService imageService = new OpenAiImageService(new OpenAiApiClientProvider(mockAIContentsModule(openAIHost), mockTokenProvider(token)));

        String url = imageService.generateImage(prompt, 1, ImageSize.Size256, ImageFormat.url).get();

        assertEquals(imageUrl, url);

        mockOpenAIServer.shutdown();
    }

    // TODO make an Object Mother to create mocks of AIContentsModule
    private static AIContentsModule mockAIContentsModule(String host) {
        AIContentsModule aiContentsModule = new AIContentsModule();
        aiContentsModule.setHost(host);

        return aiContentsModule;
    }

    // TODO make an Object Mother to create mocks of TokenProvider
    private static TokenProvider mockTokenProvider(String token){
        TokenProvider tokenProvider = mock(TokenProvider.class);
        when(tokenProvider.get()).thenReturn(token);
        return tokenProvider;
    }

    // TODO make an Object Mother to create mocks of MockWebServer
    private static MockWebServer mockupWebServer(String imageUrl) {
        MockWebServer server = new MockWebServer();
        String mockResponse = String.format("{" +
                "     \"created\": 1671045583," +
                "     \"data\": [" +
                "     {" +
                "     \"url\": \"%s\"" +
                "     }" +
                "     ]" +
                "     }", imageUrl);
        server.enqueue(new MockResponse().setHeader("Content-Type", "application/json").setBody(mockResponse));

        return server;
    }
}
