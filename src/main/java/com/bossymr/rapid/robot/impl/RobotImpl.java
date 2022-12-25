package com.bossymr.rapid.robot.impl;

import com.bossymr.rapid.language.RapidFileType;
import com.bossymr.rapid.language.symbol.RapidTask;
import com.bossymr.rapid.language.symbol.virtual.VirtualSymbol;
import com.bossymr.rapid.robot.*;
import com.bossymr.rapid.robot.network.Module;
import com.bossymr.rapid.robot.network.*;
import com.intellij.credentialStore.Credentials;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.*;

public class RobotImpl implements Robot {

    private @NotNull RobotState robotState;
    private @Nullable RobotService robotService;

    private @NotNull List<RapidTask> tasks;
    private @NotNull Map<String, VirtualSymbol> symbols;

    /**
     * Creates a new {@code Robot} which is connected to the remote robot through the specified service.
     *
     * @param robotService the robot service.
     */
    public RobotImpl(@NotNull RobotService robotService) throws IOException, InterruptedException {
        this.robotService = robotService;
        robotState = RobotUtil.getRobotState(robotService);
        RemoteService.getInstance().setRobotState(robotState);
        symbols = RobotUtil.getSymbols(robotState);
        tasks = new ArrayList<>();
        download();
        RobotUtil.reload();
        RobotEventListener.publish().afterConnect(this);
    }

    /**
     * Creates a new {@code Robot} from a persisted robot state, which is not immediately connected.
     *
     * @param robotState the persisted robot state.
     */
    public RobotImpl(@NotNull RobotState robotState) {
        this.robotState = robotState;
        RemoteService.getInstance().setRobotState(robotState);
        symbols = RobotUtil.getSymbols(robotState);
        tasks = new ArrayList<>();
        retrieve();
    }

    @Override
    public @NotNull String getName() {
        return robotState.name;
    }

    @Override
    public @NotNull URI getPath() {
        return URI.create(robotState.path);
    }

    @Override
    public @NotNull Set<VirtualSymbol> getSymbols() {
        return Set.copyOf(symbols.values());
    }

    @Override
    public @NotNull List<RapidTask> getTasks() {
        return tasks;
    }

    @Override
    public @Nullable VirtualSymbol getSymbol(@NotNull String name) throws IOException, InterruptedException {
        if (symbols.containsKey(name)) {
            return symbols.get(name);
        } else {
            RobotService robotService = getRobotService();
            if (robotService != null) {
                if (robotState.cache.contains(name)) {
                    return null;
                }
                Symbol symbol;
                try {
                    symbol = robotService.getRobotWareService().getRapidService().findSymbol("RAPID" + "/" + name).send();
                } catch (ResponseStatusException e) {
                    if (e.getStatusCode() == 400) {
                        symbol = null;
                    } else {
                        throw e;
                    }
                }
                if (symbol != null) {
                    RobotState.SymbolState storageSymbolState = RobotUtil.getSymbolState(symbol);
                    VirtualSymbol virtualSymbol = RobotUtil.getSymbol(symbol);
                    symbols.put(virtualSymbol.getName(), virtualSymbol);
                    robotState.symbols.add(storageSymbolState);
                    RobotEventListener.publish().onSymbol(this, virtualSymbol);
                    return virtualSymbol;
                } else {
                    robotState.cache.add(name);
                }
            }
        }
        return null;
    }

    @Override
    public void upload() throws IOException, InterruptedException {
        if (robotService == null) throw new IllegalStateException();
        for (RapidTask task : getTasks()) {
            Set<VirtualFile> modules = task.getFiles();
            upload(task, modules);
        }
    }

    @Override
    public void upload(@NotNull RapidTask task, @NotNull Set<VirtualFile> modules) throws IOException, InterruptedException {
        try {
            ApplicationManager.getApplication().invokeLater(() -> ApplicationManager.getApplication().runWriteAction(() -> {
                try {
                    doUpload(task, modules);
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }));
        } catch (RuntimeException e) {
            if (e.getCause() instanceof IOException) throw (IOException) e.getCause();
            if (e.getCause() instanceof InterruptedException) throw (InterruptedException) e.getCause();
            throw e;
        }
    }

    private void doUpload(@NotNull RapidTask task, @NotNull Set<VirtualFile> modules) throws IOException, InterruptedException {
        if (robotService == null) throw new IllegalStateException();
        File directory = FileUtil.createTempDirectory("robot", "upload");
        VirtualFile virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(directory);
        assert virtualFile != null;
        List<Task> remoteTasks = robotService.getRobotWareService().getRapidService().getTaskService().getTasks().send();
        for (Task remoteTask : remoteTasks) {
            if (remoteTask.getName().equals(task.getName())) {
                String programName = remoteTask.getProgram().send().getName();
                File programFile = directory.toPath().resolve(programName + ".pgf").toFile();
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(programFile))) {
                    writer.write("<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?>\r\n");
                    writer.write("<Program>\r\n");
                    for (VirtualFile module : modules) {
                        writer.write("\t<Module>" + module.getName() + "</Module>\r\n");
                        module.copy(this, virtualFile, module.getName());
                    }
                    writer.write("</Program>");
                }
                remoteTask.getProgram().send().load(programFile.getPath(), LoadProgramMode.REPLACE).send();
                return;
            }
        }
    }

    @Override
    public void download() throws IOException, InterruptedException {
        if (robotService == null) throw new IllegalStateException();
        List<RapidTask> rapidTasks = new ArrayList<>();
        Path defaultPath = Path.of(PathManager.getSystemPath(), "robot");
        File defaultFile = defaultPath.toFile();
        if (defaultFile.exists()) FileUtil.delete(defaultFile);
        if (!defaultFile.mkdir()) throw new IOException();
        List<Task> remoteTasks = robotService.getRobotWareService().getRapidService().getTaskService().getTasks().send();
        for (Task remoteTask : remoteTasks) {
            File taskFile = defaultPath.resolve(remoteTask.getName()).toFile();
            if (!taskFile.mkdir()) throw new IOException();
            Set<VirtualFile> virtualFiles = new HashSet<>();
            RapidTask rapidTask = new RapidTaskImpl(remoteTask.getName(), virtualFiles);
            List<ModuleInfo> moduleInfos = remoteTask.getModules().send();
            for (ModuleInfo moduleInfo : moduleInfos) {
                Module module = moduleInfo.getModule().send();
                module.save(module.getName(), taskFile.getPath()).send();
                VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByNioFile(taskFile.toPath().resolve(module.getName() + RapidFileType.DEFAULT_DOT_EXTENSION));
                virtualFiles.add(virtualFile);
            }
            rapidTasks.add(rapidTask);
        }
        this.tasks = rapidTasks;
    }

    public void retrieve() {
        Path defaultPath = Path.of(PathManager.getSystemPath(), "robot");
        File defaultFile = defaultPath.toFile();
        List<RapidTask> rapidTasks = new ArrayList<>();
        if (defaultFile.exists()) {
            File[] taskFiles = defaultFile.listFiles();
            if (taskFiles != null) {
                for (File taskFile : taskFiles) {
                    File[] moduleFiles = taskFile.listFiles();
                    Set<VirtualFile> virtualFiles = new HashSet<>();
                    RapidTask rapidTask = new RapidTaskImpl(taskFile.getName(), virtualFiles);
                    if (moduleFiles != null) {
                        for (File moduleFile : moduleFiles) {
                            VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByIoFile(moduleFile);
                            virtualFiles.add(virtualFile);
                        }
                    }
                    rapidTasks.add(rapidTask);
                }
            }
        }
        this.tasks = rapidTasks;
    }

    @Override
    public @Nullable RobotService getRobotService() {
        return robotService;
    }

    @Override
    public void reconnect() throws IOException, InterruptedException {
        URI path = URI.create(robotState.path);
        Credentials credentials = RobotUtil.getCredentials(path);
        if (credentials != null) {
            reconnect(credentials);
        }
    }

    @Override
    public void reconnect(@NotNull Credentials credentials) throws IOException, InterruptedException {
        RobotEventListener.publish().beforeRefresh(this);
        URI path = URI.create(robotState.path);
        RobotUtil.setCredentials(path, credentials);
        robotService = RobotService.connect(path, credentials);
        robotState = RobotUtil.getRobotState(robotService);
        RemoteService.getInstance().setRobotState(robotState);
        symbols = RobotUtil.getSymbols(robotState);
        retrieve();
        RobotUtil.reload();
        RobotEventListener.publish().afterRefresh(this);
    }

    @Override
    public void disconnect() throws IOException {
        RobotEventListener.publish().beforeDisconnect(this);
        if (getRobotService() != null) {
            getRobotService().getNetworkClient().close();
            this.robotService = null;
        }
        RobotEventListener.publish().afterDisconnect(this);
    }

    public void dispose() throws IOException {
        if (getRobotService() != null) {
            getRobotService().getNetworkClient().close();
            this.robotService = null;
        }
    }
}
