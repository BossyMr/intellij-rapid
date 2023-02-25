package com.bossymr.rapid.ide;

import com.bossymr.rapid.RapidBundle;
import com.intellij.diagnostic.IdeaReportingEvent;
import com.intellij.ide.DataManager;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.idea.IdeaLogger;
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

public class RapidErrorReportSubmitter extends ErrorReportSubmitter {

    static {
        Sentry.init(options -> options.setDsn("https://3e701f47c3e045cca57ce91c003b818c@o4504737309458432.ingest.sentry.io/4504737556987904"));
    }

    @Override
    public @NlsActions.ActionText @NotNull String getReportActionText() {
        return RapidBundle.message("error.report.author");
    }


    @Override
    public @Nullable String getPrivacyNoticeText() {
        return RapidBundle.message("error.report.notice");
    }

    @Override
    public boolean submit(IdeaLoggingEvent @NotNull [] events, @Nullable String additionalInfo, @NotNull Component parentComponent, @NotNull Consumer<? super SubmittedReportInfo> consumer) {
        DataManager dataManager = DataManager.getInstance();
        DataContext dataContext = dataManager.getDataContext(parentComponent);
        Project project = dataContext.getData(CommonDataKeys.PROJECT);
        new Task.Backgroundable(project, RapidBundle.message("error.report.task")) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                for (IdeaLoggingEvent event : events) {
                    if (event instanceof IdeaReportingEvent reportingEvent) {
                        Throwable throwable = reportingEvent.getData().getThrowable();

                        SentryEvent sentryEvent = new SentryEvent(throwable);
                        sentryEvent.setLevel(SentryLevel.ERROR);

                        IdeaPluginDescriptor descriptor = reportingEvent.getPlugin();
                        if (descriptor != null) {
                            sentryEvent.setRelease(descriptor.getVersion());
                            boolean internal = ApplicationManager.getApplication().isInternal();
                            sentryEvent.setEnvironment(internal ? "development" : "production");
                        }

                        sentryEvent.setTag("IDE", ApplicationInfo.getInstance().getBuild().asString());
                        sentryEvent.setTag("OS", SystemInfo.getOsNameAndVersion());

                        Breadcrumb breadcrumb = Breadcrumb.info(IdeaLogger.ourLastActionId);
                        breadcrumb.setCategory("action");
                        sentryEvent.addBreadcrumb(breadcrumb);

                        SentryId sentryId = Sentry.captureEvent(sentryEvent);

                        if (!(sentryId.equals(SentryId.EMPTY_ID)) && additionalInfo != null && additionalInfo.length() > 0) {
                            UserFeedback userFeedback = new UserFeedback(sentryId);
                            userFeedback.setComments(additionalInfo);
                            Sentry.captureUserFeedback(userFeedback);
                        }

                        ApplicationManager.getApplication().invokeLater(() -> {
                            if (sentryId.equals(SentryId.EMPTY_ID)) {
                                SubmittedReportInfo reportInfo = new SubmittedReportInfo(SubmittedReportInfo.SubmissionStatus.FAILED);
                                boolean result = MessageDialogBuilder.yesNo(RapidBundle.message("error.report.failed"), RapidBundle.message("error.report.failed.message")).ask(project);
                                if (result) {
                                    submit(events, additionalInfo, parentComponent, consumer);
                                } else {
                                    consumer.consume(reportInfo);
                                }
                            } else {
                                SubmittedReportInfo reportInfo = new SubmittedReportInfo(null, sentryId.toString(), SubmittedReportInfo.SubmissionStatus.NEW_ISSUE);
                                consumer.consume(reportInfo);
                                NotificationGroupManager.getInstance().getNotificationGroup("Error Report")
                                        .createNotification(RapidBundle.message("error.report.submitted"), RapidBundle.message("error.report.submitted.feedback"), NotificationType.INFORMATION)
                                        .setImportant(false)
                                        .notify(project);

                            }
                        });
                    }
                }
            }
        }.queue();
        return true;
    }
}
