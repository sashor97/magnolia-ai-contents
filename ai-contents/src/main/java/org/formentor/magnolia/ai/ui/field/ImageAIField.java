package org.formentor.magnolia.ai.ui.field;

import com.vaadin.server.FileResource;
import com.vaadin.server.Page;
import com.vaadin.server.Resource;
import com.vaadin.server.StreamResource;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.VerticalLayout;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.icons.MagnoliaIcons;
import info.magnolia.ui.ValueContext;
import info.magnolia.ui.api.message.Message;
import info.magnolia.ui.api.message.MessageType;
import info.magnolia.ui.datasource.jcr.JcrNodeWrapper;
import info.magnolia.ui.framework.message.MessagesManager;
import info.magnolia.ui.framework.util.TempFilesManager;
import info.magnolia.ui.theme.ResurfaceTheme;
import info.magnolia.ui.vaadin.server.DownloadStreamResource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.Tika;
import org.formentor.magnolia.ai.domain.ImageAiService;
import org.formentor.magnolia.ai.domain.ImageFormat;
import org.formentor.magnolia.ai.domain.ImageSize;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

import javax.inject.Inject;
import javax.jcr.RepositoryException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class ImageAIField extends CustomField<File> {
    private static final Resource DEFAULT_REVIEW_IMG = MagnoliaIcons.FILE;
    private static final Tika TIKA = new Tika();

    private final TempFilesManager tempFilesManager;
    private final String promptProperty;
    private final SimpleTranslator translator;
    private final MessagesManager messagesManager;
    private final ImageAiService imageAIService;

    private File currentTempFile;
    private CssLayout imageContainer;
    private Button removeUploadBtn;
    private Button downloadBtn;
    private Image thumbnail;

    private final ValueContext<JcrNodeWrapper> valueContext;

    @Inject
    public ImageAIField(TempFilesManager tempFilesManager, ImageAIFieldDefinition definition, SimpleTranslator translator, MessagesManager messagesManager, ImageAiService imageAIService, ValueContext<JcrNodeWrapper> valueContext) {
        this.tempFilesManager = tempFilesManager;
        this.promptProperty = definition.getPromptProperty();
        this.translator = translator;
        this.messagesManager = messagesManager;
        this.imageAIService = imageAIService;
        this.valueContext = valueContext;
    }

    @Override
    public File getValue() {
        return currentTempFile;
    }

    @Override
    protected void doSetValue(File value) {
        currentTempFile = value;
        tempFilesManager.register(value);
    }

    @Override
    protected Component initContent() {
        Optional<String> imagePrompt = getPropertyValueFromContext(valueContext, promptProperty);
        // Create body layout
        VerticalLayout rootLayout = new VerticalLayout();
        rootLayout.setSizeFull();
        rootLayout.setMargin(false);
        rootLayout.setSpacing(true);

        // Container for the image
        thumbnail = new Image(StringUtils.EMPTY, DEFAULT_REVIEW_IMG);
        thumbnail.addStyleName("file-preview-thumbnail");
        imageContainer = buildImageContainer(thumbnail, imagePrompt);

        // Container for the prompt used to create the image using AI
        String caption = imagePrompt.orElse("Write the prompt of the image in the field \"" + promptProperty + "\"");
        Label promptContainer = new Label(Jsoup.clean(caption, Safelist.basic()));
        promptContainer.setCaption(String.format("<strong>Prompt of the image (See field \"%s\")</strong>", promptProperty));
        promptContainer.setCaptionAsHtml(true);
        promptContainer.setContentMode(ContentMode.HTML);
        promptContainer.setStyleName("static-field");

        HorizontalLayout rootUploadPanel = buildRootUploadPanel();
        rootUploadPanel.addComponents(imageContainer);
        rootLayout.addComponents(rootUploadPanel, promptContainer);

        updateControlVisibilities();

        return rootLayout;
    }

    private Optional<String> getPropertyValueFromContext(ValueContext<JcrNodeWrapper> context, String propertyName) {
        return context.getSingle().flatMap(item -> {
            try {
                return Optional.ofNullable(item.getProperty(propertyName).getString());
            } catch (RepositoryException e) {
                return Optional.empty();
            } catch (Exception e) {
                log.error("Errors getting the value of the property {}", propertyName, e);
                return Optional.empty();
            }
        });
    }

    private CssLayout buildImageContainer(Image thumbnail, Optional<String> imagePrompt) {
        CssLayout imageContainer = commonUploadPanel();
        imageContainer.addComponents(buildControlButtonPanel(), thumbnail, buildButtonCreateAI(imagePrompt));

        return imageContainer;
    }

    private Button buildButtonCreateAI(Optional<String> imagePrompt) {
        Button btn = new Button("Create image AI");
        btn.addStyleName("upload-button");
        btn.addClickListener((Button.ClickListener) clickEvent -> {
            // final String title = Exceptions.wrap().get(() -> item.getProperty("title").getString());
            if (!imagePrompt.isPresent()) {
                Notification.show("Please, enter the prompt before creating the image", Notification.Type.WARNING_MESSAGE);
                return;
            }
            try {
                currentTempFile = createImageAI(imagePrompt.get()).get().orElse(null); // TODO set currentTempFile Optional
                updateControlVisibilities();
                fireEvent(createValueChange(null, false));
                Notification.show("Image created successfully");
            } catch (Exception e) {
                Notification.show("Errors creating AI image: " + e.getMessage(), Notification.Type.ERROR_MESSAGE);
            }
        });
        return btn;
    }

    private CompletableFuture<Optional<File>> createImageAI(String rawPrompt) {
        String prompt = Jsoup.clean(rawPrompt, Safelist.none());
        return imageAIService.generateImage(prompt, 1, ImageSize.Size512, ImageFormat.url)
                .thenApply(imageUrl -> {
                    if (imageUrl == null || imageUrl.isEmpty()) {
                        log.warn("Image not created");
                        return Optional.empty();
                    }
                    try {
                        File localFile = tempFilesManager.createTempFile(prompt.hashCode() + ".png");
                        final URL url = new URL(imageUrl);
                        try (ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream());
                             FileOutputStream fileOutputStream = new FileOutputStream(localFile);
                             FileChannel fileChannel = fileOutputStream.getChannel()) {
                             fileChannel.transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
                        }
                        return Optional.of(localFile);
                    } catch (IOException e) {
                        log.error("Errors creating AI image", e);
                        return Optional.empty();
                    }
                });
    }

    private void updatePreviewThumbnail() {
        if (currentTempFile != null) {
            thumbnail.setIcon(null);
            thumbnail.setSource(new FileResource(currentTempFile));
            imageContainer.removeStyleName("upload-file-panel-large");
        } else {
            thumbnail.setIcon(null);
            thumbnail.setSource(null);
            imageContainer.setStyleName("upload-file-panel");
        }
    }

    private void updateControlVisibilities() {
        boolean hasValue = getValue() != null;

        removeUploadBtn.setVisible(hasValue);
        downloadBtn.setVisible(hasValue);

        updatePreviewThumbnail();
        thumbnail.setVisible(hasValue);
    }

    private Button createControlPanelButton(Resource icon) {
        Button button = new Button(icon);
        button.addStyleNames(ResurfaceTheme.BUTTON_ICON, "control-button");
        return button;
    }

    private void clearUploadPanel() {
        FileUtils.deleteQuietly(currentTempFile);
        currentTempFile = null;
        updateControlVisibilities();
    }

    private void openDownload() {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(FileUtils.readFileToByteArray(currentTempFile))) {
            String mimeType = TIKA.detect(currentTempFile);
            StreamResource.StreamSource streamSource = () -> inputStream;
            StreamResource streamResource = new StreamResource(streamSource, "");
            streamResource.setMIMEType(mimeType);
            String fileName = currentTempFile.getName();
            DownloadStreamResource resource = new DownloadStreamResource(streamSource, fileName);
            // Accessing the DownloadStream via getStream() will set its cacheTime to whatever is set in the parent
            // StreamResource. By default it is set to 1000 * 60 * 60 * 24, thus we have to override it beforehand.
            // A negative value or zero will disable caching of this stream.
            resource.setCacheTime(-1);
            resource.getStream().setParameter("Content-Disposition", String.format("attachment; filename=\"%s\"", fileName));
            resource.setMIMEType(mimeType);
            Page.getCurrent().open(resource, null, false);
        } catch (IOException e) {
            Message message = new Message(MessageType.ERROR,
                    translator.translate("fields.uploadField.download.error.subject"),
                    translator.translate("fields.uploadField.download.error.message"));

            messagesManager.sendLocalMessage(message);
            log.warn(e.getMessage());
        }
    }

    private CssLayout buildControlButtonPanel() {
        CssLayout controlButtonPanel = new CssLayout();
        controlButtonPanel.addStyleName("control-button-panel");
        controlButtonPanel.setWidth(30, Unit.PIXELS);

        removeUploadBtn = createControlPanelButton(MagnoliaIcons.TRASH);
        removeUploadBtn.addClickListener(event -> clearUploadPanel());
        removeUploadBtn.setDescription(translator.translate("fields.uploadField.upload.removeFile"));

        controlButtonPanel.addComponent(removeUploadBtn);

        downloadBtn = createControlPanelButton(MagnoliaIcons.DOWNLOAD);
        downloadBtn.addClickListener(event -> openDownload());
        downloadBtn.setDescription(translator.translate("fields.uploadField.upload.download"));
        controlButtonPanel.addComponent(downloadBtn);
        return controlButtonPanel;
    }

    private HorizontalLayout buildRootUploadPanel() {
        HorizontalLayout rootUploadPanel = new HorizontalLayout();
        rootUploadPanel.setSizeFull();
        rootUploadPanel.setHeightUndefined();
        rootUploadPanel.setVisible(true);

        return rootUploadPanel;
    }

    private CssLayout commonUploadPanel() {
        CssLayout uploadRelatedLayout = new CssLayout();
        uploadRelatedLayout.setSizeFull();
        uploadRelatedLayout.setStyleName("upload-file-panel");
        uploadRelatedLayout.setHeightUndefined();
        return uploadRelatedLayout;
    }
}
