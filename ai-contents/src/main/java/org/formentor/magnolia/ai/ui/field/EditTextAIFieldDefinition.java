package org.formentor.magnolia.ai.ui.field;

import info.magnolia.ui.field.FieldType;
import info.magnolia.ui.field.TextFieldDefinition;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Field definition for text edit AI.
 */
@FieldType("editTextFieldAI")
@Getter
@Setter
@Slf4j
public class EditTextAIFieldDefinition extends TextFieldDefinition {
    String performance;
    public EditTextAIFieldDefinition() {
        setFactoryClass(EditTextAIFieldFactory.class);
    }
}
