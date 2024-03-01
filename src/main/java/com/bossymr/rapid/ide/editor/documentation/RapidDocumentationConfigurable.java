package com.bossymr.rapid.ide.editor.documentation;

import com.bossymr.rapid.RapidBundle;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.ui.SimpleListCellRenderer;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.function.Function;

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
    public boolean isModified() {
        RapidDocumentationState state = RapidDocumentationService.getInstance().getState();
        boolean modified = state.downloadOption != component.state.getItem();
        modified |= state.preferredLanguage != component.language.getItem();
        return modified;
    }

    @Override
    public void apply() throws ConfigurationException {
        RapidDocumentationState state = RapidDocumentationService.getInstance().getState();
        state.downloadOption = component.state.getItem();
        state.preferredLanguage = component.language.getItem();
    }

    @Override
    public void reset() {
        RapidDocumentationState state = RapidDocumentationService.getInstance().getState();
        component.state.setItem(state.downloadOption);
        component.language.setItem(state.preferredLanguage);
    }

    private static class Component {

        public final JPanel panel;
        public final JBLabel downloadPath = new JBLabel();
        public final ComboBox<RapidDocumentationState.DownloadOption> state = new ComboBox<>();
        public final ComboBox<RapidDocumentationState.Language> language = new ComboBox<>();

        public Component() {
            initializeComboBox(state, RapidDocumentationState.DownloadOption.values(), RapidDocumentationState.DownloadOption::getMessage);
            initializeComboBox(language, RapidDocumentationState.Language.values(), RapidDocumentationState.Language::getMessage);
            downloadPath.setComponentStyle(UIUtil.ComponentStyle.SMALL);
            downloadPath.setText(RapidDocumentationService.DOWNLOAD_PATH);
            this.panel = FormBuilder.createFormBuilder()
                                    .addLabeledComponent(new JBLabel(RapidBundle.message("documentation.download.state")), state)
                                    .addLabeledComponent(new JBLabel(RapidBundle.message("documentation.language.state")), language)
                                    .addComponentToRightColumn(downloadPath)
                                    .getPanel();
        }

        private <T> void initializeComboBox(@NotNull ComboBox<T> comboBox, T @NotNull [] values, @NotNull Function<T, String> message) {
            for (T value : values) {
                comboBox.addItem(value);
            }
            comboBox.setRenderer(SimpleListCellRenderer.create("", message::apply));
        }
    }
}
