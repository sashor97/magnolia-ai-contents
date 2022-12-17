package org.formentor.magnolia.ai.openai;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class ImagesGenerationRequest {

    public enum Size {
        Size256 ("256x256"),
        Size512 ("512x512"),
        Size1024 ("1024X1024");

        public final String value;
        Size (String value) {
            this.value = value;
        }
    }

    public enum ResponseFormat { url, b64_json }

    private String prompt;
    private Integer n;
    private String size; // TODO use enum Size
    private String response_format; // TODO use enum ResponseFormat
}
