package org.formentor.magnolia.ai.infrastructure.openai;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

public interface OpenAiApi {
    @POST
    @Path("/v1/images/generations")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    ImagesResponse generateImage(ImagesRequest request);

    @POST
    @Path("/v1/completions")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    CompletionResult createCompletion(CompletionRequest request);

    @POST
    @Path("/v1/edits")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    EditResult createEdit(EditRequest request);
}
