package org.formentor.magnolia.ai.openai;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ImagesGenerationResponse {
    private Integer created;
    private List<Data> data;

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class Data {
        private String url;
    }
}
