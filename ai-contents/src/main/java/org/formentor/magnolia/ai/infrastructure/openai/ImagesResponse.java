package org.formentor.magnolia.ai.infrastructure.openai;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ImagesResponse implements Serializable {
    private Integer created;
    private List<Data> data;

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class Data {
        private String url;
        private String b64_json;
    }
}
