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
import org.formentor.magnolia.ai.AIContentsModule;
import org.formentor.magnolia.ai.domain.Strategy;
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

    private final Strategy strategy;

    private final AIContentsModule aiContentsModule;

    private static final String DEFAULT_BUTTON_TEXT = "Complete text with AI";

    private static final String DEFAULT_BUTTON_EDIT_TEXT = "Fix spelling mistakes in text";

    private static final String DEFAULT_DIALOG_ID = "ai-contents:TextAIDialog";


    @Inject
    public TextAIField(AbstractTextField textField, TextAIFieldDefinition definition, DialogDefinitionRegistry dialogDefinitionRegistry, I18nizer i18nizer, UIComponent parentView, DialogBuilder dialogBuilder, TextAiService textAiService, AIContentsModule aiContentsModule) {
        this.textField = textField;
        this.strategy = getTextStrategy(definition.getStrategy());
        this.aiContentsModule = aiContentsModule;
        this.dialogCallback = new DialogCallback(dialogDefinitionRegistry, i18nizer, parentView, dialogBuilder); // TODO try to inject DialogCallback
        this.textAiService = textAiService;
        this.words = definition.getWords();
        this.performance = getTextPerformance(definition.getPerformance());
    }

    @Override
    protected Component initContent() {
        VerticalLayout layout = new VerticalLayout();
        String buttonText = DEFAULT_BUTTON_TEXT;
        if (strategy.equals(Strategy.edit)) {
            buttonText = aiContentsModule.getInstruction() != null ? aiContentsModule.getInstruction() : DEFAULT_BUTTON_EDIT_TEXT;
        }

        Button button = new Button(buttonText);
        addListenerOnButton(button, buttonText);


        layout.addComponents(textField, button);
        return layout;
    }

    public void addListenerOnButton(Button button, String instruction) {
        if (strategy.equals(Strategy.edit)) {
            button.addClickListener((Button.ClickListener) event -> dialogCallback.open(
                    DEFAULT_DIALOG_ID,
                    properties -> textAiService.editText(properties.get("prompt").toString(), performance, instruction).thenAccept(textField::setValue)
            ));
        } else {
            button.addClickListener((Button.ClickListener) event -> dialogCallback.open(
                    DEFAULT_DIALOG_ID,
                    properties -> textAiService.completeText(properties.get("prompt").toString(), words, performance).thenAccept(textField::setValue)
            ));
        }
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
            return performance == null ? null : TextPerformance.valueOf(performance);
        } catch (IllegalArgumentException e) {
            log.warn("Text performance {} not allowed", performance);
            return null;
        }
    }

    public Strategy getTextStrategy(String strategy) {
        try {
            return strategy == null ? null : Strategy.valueOf(strategy);
        } catch (IllegalArgumentException e) {
            log.warn("Text strategy {} not allowed", strategy);
            return null;
        }
    }
}
