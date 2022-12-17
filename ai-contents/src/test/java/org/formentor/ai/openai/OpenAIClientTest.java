package org.formentor.ai.openai;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.apache.logging.log4j.util.Strings;
import org.formentor.magnolia.ai.openai.ImagesGenerationRequest.Size;
import org.formentor.magnolia.ai.openai.OpenAIClient;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OpenAIClientTest {

    @Test
    public void generateImage_should_post_request_to_OpenApi() throws IOException, InterruptedException {
        final String prompt = "A cute baby sea otter";
        MockWebServer mockOpenAIServer = mockupWebServer("does-not-mind");

        final String openApiHost = mockOpenAIServer.url(Strings.EMPTY).toString();
        final String token = "sk-...DeVF";
        OpenAIClient openAIClient = new OpenAIClient(openApiHost, token);

        openAIClient.generateImageFormatUrl(prompt, 1, Size.Size256).join();

        RecordedRequest request = mockOpenAIServer.takeRequest();
        assertEquals("POST", request.getMethod());
        assertEquals("/v1/images/generations", request.getPath());
        assertEquals("{\"prompt\":\"A cute baby sea otter\",\"n\":1,\"size\":\"256x256\",\"response_format\":\"url\"}", request.getBody().readUtf8());
        assertEquals(String.format("Bearer %s", token), request.getHeader("Authorization"));

        mockOpenAIServer.shutdown();
    }

    @Test
    public void generateImage_should_return_url_to_image() throws ExecutionException, InterruptedException, IOException {
        final String prompt = "A cute baby sea otter";
        final String imageUrl = "https://oaidalleapiprodscus.blob.core.windows.net/private/org-x/user-j/img-Io.png?st=2022-12-14T18%3A19%3A43Z&skv=2021-08-06&sig=A%3D";
        MockWebServer mockOpenAIServer = mockupWebServer(imageUrl);

        final String openApiHost = mockOpenAIServer.url(Strings.EMPTY).toString();
        final String token = "sk-...DeVF";
        OpenAIClient openAIClient = new OpenAIClient(openApiHost, token);

        String url = openAIClient.generateImageFormatUrl(prompt, 1, Size.Size256)
                .thenApply(imagesGenerationResponse -> imagesGenerationResponse.getData().get(0).getUrl())
                .get();

        mockOpenAIServer.shutdown();

        assertEquals(imageUrl, url);
    }

    private static MockWebServer mockupWebServer(String imageUrl) {
        MockWebServer mockWebServer = new MockWebServer();
        String mockResponse = String.format("{" +
                "     \"created\": 1671045583," +
                "     \"data\": [" +
                "     {" +
                "     \"url\": \"%s\"" +
                "     }" +
                "     ]" +
                "     }", imageUrl);
        mockWebServer.enqueue(new MockResponse().setBody(mockResponse));

        return mockWebServer;
    }
}
