package com.bossymr.rapid.robot;

import com.bossymr.rapid.language.symbol.RapidTask;
import com.bossymr.rapid.robot.network.Module;
import com.bossymr.rapid.robot.network.*;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileDocumentManagerListener;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class RobotFileDocumentManagerListener implements FileDocumentManagerListener {

    @Override
    public void beforeDocumentSaving(@NotNull Document document) {
        save(document);
    }

    private void save(@NotNull Document document) {
        VirtualFile virtualFile = getVirtualFile(document);
        String body = document.getText();
        if (virtualFile == null) return;
        RapidTask task = getTask(virtualFile);
        RobotService robotService = getRobotService();
        if (task != null && robotService != null) {
            getTasks(robotService, task)
                    .thenComposeAsync(entity -> entity == null ?
                            CompletableFuture.completedFuture(null) :
                            getModule(entity, virtualFile)
                                    .thenComposeAsync(response -> response == null ?
                                            CompletableFuture.completedFuture(null) :
                                            save(response, body)));
        }
    }

    private @NotNull CompletableFuture<@Nullable Task> getTasks(@NotNull RobotService robotService, @NotNull RapidTask task) {
        return robotService.getRobotWareService().getRapidService().getTaskService().getTasks().sendAsync()
                .thenApplyAsync(entities -> {
                    for (Task entity : entities) {
                        if (entity.getName().equals(task.getName())) {
                            return entity;
                        }
                    }
                    return null;
                });
    }

    private @NotNull CompletableFuture<@Nullable Module> getModule(@NotNull Task task, @NotNull VirtualFile virtualFile) {
        return task.getModules().sendAsync()
                .thenApplyAsync(moduleInfos -> {
                    for (ModuleInfo moduleInfo : moduleInfos) {
                        if (moduleInfo.getName().equals(virtualFile.getNameWithoutExtension())) {
                            return moduleInfo;
                        }
                    }
                    return null;
                }).thenComposeAsync(moduleInfo -> moduleInfo != null ?
                        moduleInfo.getModule().sendAsync() :
                        CompletableFuture.completedFuture(null));
    }

    private @NotNull CompletableFuture<UpdateModuleText> save(@NotNull Module module, @NotNull String text) {
        text = text.replaceAll("\r\n", "\n");
        return module.setText(text).sendAsync();
    }

    private @Nullable RobotService getRobotService() {
        RemoteService remoteService = RemoteService.getInstance();
        Robot robot = remoteService.getRobot();
        if (robot != null) {
            return robot.getRobotService();
        }
        return null;
    }

    private @Nullable RapidTask getTask(@NotNull VirtualFile virtualFile) {
        RemoteService remoteService = RemoteService.getInstance();
        Robot robot = remoteService.getRobot();
        if (robot != null) {
            for (RapidTask task : robot.getTasks()) {
                if (task.getFiles().contains(virtualFile)) {
                    return task;
                }
            }
        }
        return null;
    }

    private @Nullable VirtualFile getVirtualFile(@NotNull Document document) {
        FileDocumentManager fileDocumentManager = FileDocumentManager.getInstance();
        return fileDocumentManager.getFile(document);
    }


}
