package org.formentor.magnolia.ai.openai;

import info.magnolia.init.MagnoliaConfigurationProperties;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.function.Supplier;

/**
 * Provider for the OpenAI token.
 *
 * This implementation fetches the token from environment, in case you are using Magnolia Passwords, inject the required implementation.
 */
@Singleton
public class OpenAITokenProvider implements Supplier<String> {

    private final String token;

    @Inject
    public OpenAITokenProvider(MagnoliaConfigurationProperties configurationProperties) {
        token = configurationProperties.getProperty("OPENAI_TOKEN");
    }

    @Override
    public String get() {
        return token;
    }
}
