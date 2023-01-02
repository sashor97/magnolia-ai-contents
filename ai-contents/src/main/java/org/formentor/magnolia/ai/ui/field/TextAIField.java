package org.formentor.magnolia.ai.ui.field;

import com.vaadin.ui.AbstractTextField;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.VerticalLayout;
import info.magnolia.i18nsystem.I18nizer;
import info.magnolia.ui.UIComponent;
import info.magnolia.ui.dialog.DialogBuilder;
import info.magnolia.ui.dialog.DialogDefinitionRegistry;
import lombok.extern.slf4j.Slf4j;
import org.formentor.magnolia.ai.domain.TextAiService;
import org.formentor.magnolia.ai.domain.TextPerformance;
import org.formentor.magnolia.ai.ui.dialog.DialogCallback;

import javax.inject.Inject;
import java.util.Optional;

@Slf4j
public class TextAIField extends CustomField<String> {

    private final AbstractTextField textField;
    private final DialogCallback dialogCallback;
    private final TextAiService textAiService;
    private final Integer words;
    private final TextPerformance performance;

    @Inject
    public TextAIField(AbstractTextField textField, TextAIFieldDefinition definition, DialogDefinitionRegistry dialogDefinitionRegistry, I18nizer i18nizer, UIComponent parentView, DialogBuilder dialogBuilder, TextAiService textAiService) {
        this.textField = textField;
        this.dialogCallback = new DialogCallback(dialogDefinitionRegistry, i18nizer, parentView, dialogBuilder); // TODO try to inject DialogCallback
        this.textAiService = textAiService;
        this.words = definition.getWords();
        this.performance = getTextPerformance(definition.getPerformance());
    }

    @Override
    protected Component initContent() {
        VerticalLayout layout = new VerticalLayout();
        Button button = new Button("Complete text with AI");
        button.addClickListener((Button.ClickListener) event -> dialogCallback.open(
                "ai-contents:TextAIDialog",
                properties -> textAiService.completeText(properties.get("prompt").toString(), words, performance).thenAccept(textField::setValue)
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
