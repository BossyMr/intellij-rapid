package com.bossymr.rapid.ide;

import com.bossymr.rapid.RapidBundle;
import com.intellij.diagnostic.AbstractMessage;
import com.intellij.diagnostic.IdeaReportingEvent;
import com.intellij.ide.DataManager;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.ErrorReportSubmitter;
import com.intellij.openapi.diagnostic.IdeaLoggingEvent;
import com.intellij.openapi.diagnostic.SubmittedReportInfo;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageDialogBuilder;
import com.intellij.openapi.util.NlsActions;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.util.Consumer;
import io.sentry.*;
import io.sentry.protocol.SentryId;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.List;

/**
 * An {@code ErrorReportSubmitter} enables the ability to report exceptions to the plugin author.
 * <p>
 * This report submitter uses the Sentry SDK to submit exceptions to Sentry.
 *
 * @see <a href="https://plugins.jetbrains.com/docs/intellij/ide-infrastructure.html#error-reporting">Intellij Platform
 * Plugin SDK: Error Reporting</a>
 */
public class RapidErrorReportSubmitter extends ErrorReportSubmitter {

    private static final @NotNull String SENTRY_URL = "https://770a26d5df85543f57313f65d4474df5@sentry.bossymr.com/2";

    @Override
    public @NlsActions.ActionText @NotNull String getReportActionText() {
        return RapidBundle.message("error.report.author");
    }


    @Override
    public @Nullable String getPrivacyNoticeText() {
        return RapidBundle.message("error.report.privacy.notice");
    }

    @Override
    public boolean submit(IdeaLoggingEvent @NotNull [] events, @Nullable String additionalInfo, @NotNull Component parentComponent, @NotNull Consumer<? super SubmittedReportInfo> consumer) {
        DataManager dataManager = DataManager.getInstance();
        DataContext dataContext = dataManager.getDataContext(parentComponent);
        Project project = dataContext.getData(CommonDataKeys.PROJECT);
        new Task.Backgroundable(project, RapidBundle.message("error.report.background.task.name")) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                for (IdeaLoggingEvent event : events) {
                    if (!(event instanceof IdeaReportingEvent reportingEvent)) {
                        continue;
                    }
                    AbstractMessage message = reportingEvent.getData();
                    Throwable throwable = message.getThrowable();

                    List<Attachment> attachments = message.getIncludedAttachments().stream()
                            .map(attachment -> new Attachment(attachment.getBytes(), attachment.getName()))
                            .toList();

                    SentryEvent sentryEvent = new SentryEvent(throwable);
                    sentryEvent.setLevel(SentryLevel.ERROR);
                    sentryEvent.setUser(null);

                    IdeaPluginDescriptor descriptor = reportingEvent.getPlugin();
                    if (descriptor != null) {
                        sentryEvent.setRelease(descriptor.getVersion());
                    }

                    sentryEvent.setTag("IDE", ApplicationInfo.getInstance().getBuild().asString());
                    sentryEvent.setTag("OS", SystemInfo.getOsNameAndVersion());
                    sentryEvent.setEnvironment(ApplicationManager.getApplication().isInternal() ? "development" : "production");

                    SentryOptions options = new SentryOptions();
                    options.setDsn(SENTRY_URL);
                    Hub hub = new Hub(options);

                    SentryId sentryId = hub.captureEvent(sentryEvent, Hint.withAttachments(attachments));

                    if (!(sentryId.equals(SentryId.EMPTY_ID))) {
                        if (additionalInfo != null && !(additionalInfo.isEmpty())) {
                            UserFeedback userFeedback = new UserFeedback(sentryId);
                            userFeedback.setComments(additionalInfo);
                            hub.captureUserFeedback(userFeedback);
                        }
                    }

                    hub.close();

                    ApplicationManager.getApplication().invokeLater(() -> {
                        if (sentryId.equals(SentryId.EMPTY_ID)) {
                            // The request was unsuccessful
                            SubmittedReportInfo reportInfo = new SubmittedReportInfo(SubmittedReportInfo.SubmissionStatus.FAILED);
                            boolean retry = MessageDialogBuilder.yesNo(RapidBundle.message("error.report.failed.title"), RapidBundle.message("error.report.failed.content")).ask(project);
                            if (retry) {
                                submit(events, additionalInfo, parentComponent, consumer);
                            } else {
                                consumer.consume(reportInfo);
                            }
                        } else {
                            // The request was successful
                            SubmittedReportInfo reportInfo = new SubmittedReportInfo(null, sentryId.toString(), SubmittedReportInfo.SubmissionStatus.NEW_ISSUE);
                            consumer.consume(reportInfo);
                            NotificationGroupManager.getInstance().getNotificationGroup("Error Report")
                                    .createNotification(RapidBundle.message("error.report.submitted.title"), RapidBundle.message("error.report.submitted.content"), NotificationType.INFORMATION)
                                    .setImportant(false)
                                    .notify(project);
                        }
                    });
                }
            }
        }.queue();
        return true;
    }
}
