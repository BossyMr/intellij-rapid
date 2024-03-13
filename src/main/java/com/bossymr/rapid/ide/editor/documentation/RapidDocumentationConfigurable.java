package com.bossymr.rapid.ide.editor.documentation;

import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.ide.editor.documentation.RapidDocumentationState.DownloadOption;
import com.bossymr.rapid.ide.editor.documentation.RapidDocumentationState.Language;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.ui.EnumComboBoxModel;
import com.intellij.ui.SimpleListCellRenderer;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class RapidDocumentationConfigurable implements Configurable {

    private Component component;

    @Override
    public @NlsContexts.ConfigurableName String getDisplayName() {
        return RapidBundle.message("documentation.settings.name");
    }

    @Override
    public @Nullable JComponent createComponent() {
        return (component = new Component()).panel;
    }

    @Override
    public void disposeUIResources() {
        this.component = null;
    }

    @Override
    public boolean isModified() {
        RapidDocumentationState state = RapidDocumentationService.getInstance().getState();
        boolean modified = state.downloadOption != component.getDownloadOption();
        modified |= state.preferredLanguage != component.getLanguage();
        return modified;
    }

    @Override
    public void apply() {
        RapidDocumentationService service = RapidDocumentationService.getInstance();
        RapidDocumentationState state = service.getState();
        state.downloadOption = component.getDownloadOption();
        Language newLanguage = component.getLanguage();
        boolean isLanguageModified = state.preferredLanguage != newLanguage;
        state.preferredLanguage = newLanguage;
        if (isLanguageModified) {
            service.prepareDocumentation(null, true);
        }
    }

    @Override
    public void reset() {
        RapidDocumentationState state = RapidDocumentationService.getInstance().getState();
        component.setDownloadOption(state.downloadOption);
        component.setLanguage(state.preferredLanguage);
    }

    private static class Component {

        public final JPanel panel;
        public final JBLabel downloadPath = new JBLabel();
        public final ComboBox<DownloadOption> downloadOption = new ComboBox<>(new EnumComboBoxModel<>(DownloadOption.class));
        public final ComboBox<Language> language = new ComboBox<>(new EnumComboBoxModel<>(Language.class));

        public Component() {
            this.downloadOption.setRenderer(SimpleListCellRenderer.create("", DownloadOption::getMessage));
            this.language.setRenderer(SimpleListCellRenderer.create("", Language::getMessage));
            downloadPath.setComponentStyle(UIUtil.ComponentStyle.MINI);
            downloadPath.setText(RapidDocumentationService.DOWNLOAD_PATH);
            this.panel = createPanel();
        }

        private @NotNull JPanel createPanel() {
            return FormBuilder.createFormBuilder()
                              .addLabeledComponent(new JBLabel(RapidBundle.message("documentation.download.state")), downloadOption)
                              .addLabeledComponent(new JBLabel(RapidBundle.message("documentation.language.state")), language)
                              .addComponentToRightColumn(downloadPath)
                              .addComponentFillVertically(new JBLabel(), 0)
                              .getPanel();
        }

        public @NotNull DownloadOption getDownloadOption() {
            return downloadOption.getItem();
        }

        public void setDownloadOption(@NotNull DownloadOption downloadOption) {
            this.downloadOption.setItem(downloadOption);
        }

        public @NotNull Language getLanguage() {
            return language.getItem();
        }

        public void setLanguage(@NotNull Language language) {
            this.language.setItem(language);
        }
    }
}
