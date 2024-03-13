package com.bossymr.rapid.ide.editor.documentation;

import com.bossymr.rapid.RapidBundle;
import org.jetbrains.annotations.NotNull;

public class RapidDocumentationState {

    public @NotNull RapidDocumentationState.DownloadOption downloadOption = DownloadOption.ASK;

    public @NotNull Language preferredLanguage = Language.ENGLISH;

    public enum Language {
        CHINESE("zh-CN", "中文"),
        ITALIAN("it", "Italiano"),
        FRENCH("fr", "Français"),
        SPANISH("es", "Español"),
        ENGLISH("en", "English"),
        GERMAN("de", "Deutsch"),
        CZECH("cs", "Čeština");

        private final String code;
        private final String message;

        Language(@NotNull String code, @NotNull String message) {
            this.code = code;
            this.message = message;
        }

        public @NotNull String getCode() {
            return code;
        }

        public @NotNull String getMessage() {
            return message;
        }
    }

    public enum DownloadOption {

        ALWAYS(RapidBundle.message("documentation.settings.download.always")),
        ASK(RapidBundle.message("documentation.settings.download.ask")),
        NEVER(RapidBundle.message("documentation.settings.download.never"));

        private final @NotNull String message;

        DownloadOption(@NotNull String message) {
            this.message = message;
        }

        public @NotNull String getMessage() {
            return message;
        }
    }
}
