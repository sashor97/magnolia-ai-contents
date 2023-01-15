package org.formentor.magnolia.ai.ui.field;

import com.vaadin.ui.Component;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.field.TextFieldDefinition;
import info.magnolia.ui.field.factory.TextFieldFactory;

public class EditTextAIFieldFactory extends TextFieldFactory {
    public EditTextAIFieldFactory(TextFieldDefinition definition, ComponentProvider componentProvider) {
        super(definition, componentProvider);
    }
    @Override
    public Component createFieldComponent() {
        return componentProvider.newInstance(EditTextAIField.class, getDefinition(), super.createFieldComponent());
    }
}
