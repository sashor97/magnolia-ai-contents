package org.formentor.magnolia.ai.ui.field;

import info.magnolia.ui.field.FieldType;
import info.magnolia.ui.field.TextFieldDefinition;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

/**
 * Field definition for Image AI.
 */
@FieldType("textFieldAI")
@Getter
@Setter
@Slf4j
public class TextAIFieldDefinition extends TextFieldDefinition {
    Integer words;
    String performance;
    String strategy;
    public TextAIFieldDefinition() {
        setFactoryClass(TextAIFieldFactory.class);
    }
}
