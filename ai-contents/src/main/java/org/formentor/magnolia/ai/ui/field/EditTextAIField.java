package org.formentor.magnolia.ai.ui.field;

import com.vaadin.ui.*;
import info.magnolia.i18nsystem.I18nizer;
import info.magnolia.ui.UIComponent;
import info.magnolia.ui.dialog.DialogBuilder;
import info.magnolia.ui.dialog.DialogDefinitionRegistry;
import lombok.extern.slf4j.Slf4j;
import org.formentor.magnolia.ai.AIContentsModule;
import org.formentor.magnolia.ai.domain.TextEditAiService;
import org.formentor.magnolia.ai.domain.TextPerformance;
import org.formentor.magnolia.ai.ui.dialog.DialogCallback;

import javax.inject.Inject;
import java.util.Optional;

@Slf4j
public class EditTextAIField extends CustomField<String> {

    private final AbstractTextField textField;
    private final DialogCallback dialogCallback;
    private final TextEditAiService textEditAiService;
    private final TextPerformance performance;

    private final AIContentsModule aiContentsModule;

    private static final String INSTRUCTION_DEFAULT="Fix spelling mistakes in text";

    @Inject
    public EditTextAIField(AbstractTextField textField, EditTextAIFieldDefinition definition, DialogDefinitionRegistry dialogDefinitionRegistry, I18nizer i18nizer, UIComponent parentView, DialogBuilder dialogBuilder, TextEditAiService textEditAiService, AIContentsModule aiContentsModule) {
        this.textField = textField;
        this.aiContentsModule = aiContentsModule;
        this.dialogCallback = new DialogCallback(dialogDefinitionRegistry, i18nizer, parentView, dialogBuilder); // TODO try to inject DialogCallback
        this.textEditAiService = textEditAiService;
        this.performance = getTextPerformance(definition.getPerformance());
    }

    @Override
    protected Component initContent() {
        String instructionText=aiContentsModule.getInstruction()!=null?aiContentsModule.getInstruction():INSTRUCTION_DEFAULT;
        VerticalLayout layout = new VerticalLayout();
        Button button = new Button(instructionText);
        button.addClickListener((Button.ClickListener) event -> dialogCallback.open(
                "ai-contents:EditTextAIDialog",
                properties -> textEditAiService.editText(properties.get("prompt").toString(),performance).thenAccept(textField::setValue)
        ));

        layout.addComponents(textField, button);
        return layout;
    }

    @Override
    protected void doSetValue(String value) {
        textField.setValue(Optional.ofNullable(value).orElse(""));
    }

    @Override
    public String getValue() {
        return textField.getValue();
    }

    public TextPerformance getTextPerformance(String performance) {
        try {
            return performance == null? null: TextPerformance.valueOf(performance);
        } catch (IllegalArgumentException e) {
            log.warn("Text performance {} not allowed", performance);
            return null;
        }
    }
}
