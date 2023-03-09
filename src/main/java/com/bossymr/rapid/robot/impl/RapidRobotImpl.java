package com.bossymr.rapid.robot.impl;

import com.bossymr.network.ResponseStatusException;
import com.bossymr.network.client.NetworkEngine;
import com.bossymr.network.client.security.Credentials;
import com.bossymr.rapid.RapidIcons;
import com.bossymr.rapid.language.RapidFileType;
import com.bossymr.rapid.language.symbol.RapidRobot;
import com.bossymr.rapid.language.symbol.RapidTask;
import com.bossymr.rapid.language.symbol.virtual.VirtualSymbol;
import com.bossymr.rapid.robot.RemoteRobotService;
import com.bossymr.rapid.robot.RobotEventListener;
import com.bossymr.rapid.robot.RobotState;
import com.bossymr.rapid.robot.network.LoadProgramMode;
import com.bossymr.rapid.robot.network.RobotService;
import com.bossymr.rapid.robot.network.robotware.mastership.CloseableMastership;
import com.bossymr.rapid.robot.network.robotware.mastership.MastershipService;
import com.bossymr.rapid.robot.network.robotware.mastership.MastershipType;
import com.bossymr.rapid.robot.network.robotware.rapid.symbol.SymbolModel;
import com.bossymr.rapid.robot.network.robotware.rapid.task.Task;
import com.bossymr.rapid.robot.network.robotware.rapid.task.TaskService;
import com.bossymr.rapid.robot.network.robotware.rapid.task.module.ModuleInfo;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class RapidRobotImpl implements RapidRobot, Disposable {

    private static final Logger logger = Logger.getInstance(RapidRobotImpl.class);

    private @NotNull RobotState robotState;
    private @Nullable NetworkEngine networkEngine;

    private @NotNull List<RapidTask> tasks;
    private @NotNull Map<String, VirtualSymbol> symbols;

    /**
     * Creates a new connected {@code RapidRobotImpl} which will connect with the specified robot and credentials.
     *
     * @param path the robot path.
     * @param username the robot username.
     * @param password the robot password.
     * @throws IOException
     * @throws InterruptedException
     */
    public RapidRobotImpl(@NotNull URI path, @NotNull String username, char @NotNull [] password) throws IOException, InterruptedException {
        NetworkEngine engine = new NetworkEngine(path, () -> new Credentials(username, password));
        this.networkEngine = new RobotDelegatingNetworkEngine(engine);
        robotState = RobotUtil.getRobotState(networkEngine);
        RemoteRobotService.getInstance().setRobotState(robotState);
        symbols = RobotUtil.getSymbols(networkEngine, robotState);
        tasks = new ArrayList<>();
        try {
            download().get();
        } catch (ExecutionException e) {
            throw new IOException(e);
        }
        RobotUtil.reload();
        RobotEventListener.publish().afterConnect(this);
    }

    /**
     * Creates a new unconnected {@code RapidRobotImpl} with the specified state.
     *
     * @param robotState the robot state.
     */
    public RapidRobotImpl(@NotNull RobotState robotState) {
        this.robotState = robotState;
        RemoteRobotService.getInstance().setRobotState(robotState);
        symbols = RobotUtil.getSymbols(null, robotState);
        tasks = new ArrayList<>();
        retrieve();
    }

    @Override
    public @NotNull URI getPath() {
        return URI.create(robotState.path);
    }

    @Override
    public @NotNull String getName() {
        return robotState.name;
    }

    @Override
    public @NotNull Icon getIcon() {
        return RapidIcons.ROBOT_ICON;
    }

    @Override
    public @NotNull Set<VirtualSymbol> getSymbols() {
        return Set.copyOf(symbols.values());
    }

    @Override
    public @Nullable RapidTask getTask(@NotNull String name) {
        for (RapidTask task : tasks) {
            if (task.getName().equals(name)) {
                return task;
            }
        }
        return null;
    }

    @Override
    public @NotNull List<RapidTask> getTasks() {
        return tasks;
    }

    @Override
    public @Nullable VirtualSymbol getSymbol(@NotNull String name) {
        name = name.toLowerCase();
        if (symbols.containsKey(name)) {
            return symbols.get(name);
        } else {
            if (getNetworkEngine() != null) {
                RobotService robotService = getNetworkEngine().createService(RobotService.class);
                if (robotState.cache.contains(name)) {
                    return null;
                }
                SymbolModel symbolModel;
                try {
                    symbolModel = robotService.getRobotWareService().getRapidService().findSymbol("RAPID" + "/" + name).send();
                } catch (ResponseStatusException e) {
                    if (e.getResponse().statusCode() == 400) {
                        symbolModel = null;
                    } else {
                        return null;
                    }
                } catch (IOException | InterruptedException e) {
                    return null;
                }
                if (symbolModel != null) {
                    RobotState.SymbolState storageSymbolState = RobotUtil.getSymbolState(symbolModel);
                    VirtualSymbol virtualSymbol = RobotUtil.getSymbol(symbolModel);
                    symbols.put(virtualSymbol.getName(), virtualSymbol);
                    robotState.symbolStates.add(storageSymbolState);
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
    public @NotNull CompletableFuture<Void> upload() {
        List<CompletableFuture<Void>> requests = new ArrayList<>();
        for (RapidTask task : getTasks()) {
            Set<VirtualFile> modules = task.getFiles().stream()
                    .map(file -> LocalFileSystem.getInstance().findFileByIoFile(file))
                    .collect(Collectors.toSet());
            requests.add(upload(task, modules));
        }
        return CompletableFuture.allOf(requests.toArray(CompletableFuture[]::new));
    }

    private @NotNull CompletableFuture<Void> refreshAsync(@NotNull Collection<VirtualFile> modules) {
        List<CompletableFuture<Void>> completableFutures = new ArrayList<>();
        for (VirtualFile module : modules) {
            CompletableFuture<Void> completableFuture = new CompletableFuture<>();
            module.refresh(true, true, () -> completableFuture.complete(null));
            completableFutures.add(completableFuture);
        }
        return CompletableFuture.allOf(completableFutures.toArray(CompletableFuture[]::new));
    }

    @Override
    public @NotNull CompletableFuture<Void> upload(@NotNull RapidTask task, @NotNull Collection<VirtualFile> modules) {
        if (networkEngine == null) {
            return CompletableFuture.failedFuture(new IllegalStateException("Robot is not connected"));
        }
        VirtualFile temporaryDirectory;
        try {
            temporaryDirectory = createTemporaryDirectory("upload");
        } catch (IOException e) {
            return CompletableFuture.failedFuture(e);
        }
        return refreshAsync(modules)
                .thenComposeAsync(unused -> networkEngine.createService(TaskService.class).getTask(task.getName()).sendAsync()
                        .thenComposeAsync(remote -> remote.getProgram().sendAsync())
                        .thenComposeAsync(program -> {
                            File file = temporaryDirectory.toNioPath().resolve(program.getName() + ".pgf").toFile();
                            WriteAction.runAndWait(() -> {
                                try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                                    writer.write("<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?>\r\n");
                                    writer.write("<Program>\r\n");
                                    for (VirtualFile module : modules) {
                                        writer.write("\t<Module>" + module.getName() + "</Module>\r\n");
                                        module.copy(this, temporaryDirectory, module.getName());
                                    }
                                    writer.write("</Program>");
                                } catch (IOException e) {
                                    throw new CompletionException(e);
                                }
                            });
                            return networkEngine.createService(MastershipService.class).getDomain(MastershipType.RAPID).sendAsync()
                                    .thenComposeAsync(domain -> CloseableMastership.requestAsync(domain, () -> program.load(file.getPath(), LoadProgramMode.REPLACE).sendAsync()));
                        }).handleAsync((ignored, throwable) -> {
                            WriteAction.runAndWait(() -> FileUtil.delete(temporaryDirectory.toNioPath().toFile()));
                            if (throwable != null) {
                                logger.error(throwable);
                            }
                            return null;
                        }));
    }

    private @NotNull VirtualFile createTemporaryDirectory(@NotNull String suffix) throws IOException {
        File file = FileUtil.createTempDirectory("intellij-rapid", suffix);
        File[] files = file.listFiles();
        if (files == null) {
            throw new IOException();
        }
        for (File child : files) {
            FileUtil.delete(child);
        }
        VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByIoFile(file);
        if (virtualFile == null) {
            throw new IOException();
        }
        return virtualFile;
    }

    @Override
    public @NotNull CompletableFuture<Void> download() {
        if (networkEngine == null) {
            throw new IllegalStateException();
        }
        VirtualFile temporaryDirectory;
        try {
            temporaryDirectory = createTemporaryDirectory("download");
        } catch (IOException e) {
            return CompletableFuture.failedFuture(e);
        }
        File file = Path.of(PathManager.getSystemPath(), "robot").toFile();
        if (file.exists()) {
            FileUtil.delete(file);
        }
        VirtualFile finalDirectory = LocalFileSystem.getInstance().findFileByNioFile(PathManager.getSystemDir());
        if (finalDirectory == null) {
            throw new IllegalStateException();
        }
        List<RapidTask> updated = new ArrayList<>();
        return networkEngine.createService(TaskService.class).getTasks().sendAsync()
                .thenComposeAsync(tasks -> {
                    List<CompletableFuture<Void>> completableFutures = new ArrayList<>();
                    for (Task task : tasks) {
                        File temporaryTask = temporaryDirectory.toNioPath().resolve(task.getName()).toFile();
                        File finalTask = finalDirectory.toNioPath().resolve("robot").resolve(task.getName()).toFile();
                        if (!(WriteAction.computeAndWait(() -> FileUtil.createDirectory(temporaryTask)))) {
                            return CompletableFuture.failedFuture(new IOException());
                        }
                        RapidTask local = new RapidTaskImpl(task.getName(), finalTask, new HashSet<>());
                        completableFutures.add(task.getModules().sendAsync()
                                .thenComposeAsync(moduleInfos -> {
                                    List<CompletableFuture<Void>> moduleEntities = new ArrayList<>();
                                    for (ModuleInfo moduleInfo : moduleInfos) {
                                        moduleEntities.add(moduleInfo.getModule().sendAsync()
                                                .thenComposeAsync(module -> module.save(module.getName(), temporaryTask.toPath().toString()).sendAsync()
                                                        .thenRunAsync(() -> {
                                                            File result = finalTask.toPath().resolve(module.getName() + RapidFileType.DEFAULT_DOT_EXTENSION).toFile();
                                                            local.getFiles().add(result);
                                                        })));
                                    }
                                    return CompletableFuture.allOf(moduleEntities.toArray(CompletableFuture[]::new));
                                }).thenRunAsync(() -> updated.add(local)));
                    }
                    return CompletableFuture.allOf(completableFutures.toArray(CompletableFuture[]::new));
                }).thenRunAsync(() -> {
                    try {
                        WriteAction.runAndWait(() -> {
                            VirtualFile directoryChild = finalDirectory.findChild("robot");
                            if (directoryChild != null) {
                                directoryChild.delete(this);
                            }
                            temporaryDirectory.copy(this, finalDirectory, "robot");
                        });
                        this.tasks = updated;
                    } catch (IOException e) {
                        throw new IllegalStateException(e);
                    }
                }).handleAsync((response, throwable) -> {
                    try {
                        WriteAction.runAndWait(() -> temporaryDirectory.delete(this));
                    } catch (IOException e) {
                        logger.error(e);
                    }
                    if (throwable != null) {
                        logger.error(throwable);
                    }
                    return null;
                });
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
                    Set<File> virtualFiles = new HashSet<>();
                    RapidTask rapidTask = new RapidTaskImpl(taskFile.getName(), taskFile, virtualFiles);
                    if (moduleFiles != null) {
                        virtualFiles.addAll(Arrays.asList(moduleFiles));
                    }
                    rapidTasks.add(rapidTask);
                }
            }
        }
        this.tasks = rapidTasks;
    }

    @Override
    public boolean isConnected() {
        return getNetworkEngine() != null;
    }

    @Override
    public @Nullable RobotService getRobotService() {
        if (networkEngine != null) {
            return networkEngine.createService(RobotService.class);
        } else {
            return null;
        }
    }

    public @Nullable NetworkEngine getNetworkEngine() {
        return networkEngine;
    }

    @Override
    public @NotNull RobotService reconnect() throws IOException, InterruptedException {
        URI path = URI.create(robotState.path);
        Credentials credentials = RobotUtil.getCredentials(path);
        if (credentials != null) {
            return reconnect(credentials);
        } else {
            throw new IllegalStateException();
        }
    }

    @Override
    public @NotNull RobotService reconnect(@NotNull Credentials credentials) throws IOException, InterruptedException {
        RobotEventListener.publish().beforeRefresh(this);
        URI path = URI.create(robotState.path);
        RobotUtil.setCredentials(path, credentials.username(), credentials.password());
        NetworkEngine engine = new NetworkEngine(path, () -> credentials);
        this.networkEngine = new RobotDelegatingNetworkEngine(engine);
        robotState = RobotUtil.getRobotState(networkEngine);
        RemoteRobotService.getInstance().setRobotState(robotState);
        symbols = RobotUtil.getSymbols(networkEngine, robotState);
        retrieve();
        RobotUtil.reload();
        RobotEventListener.publish().afterRefresh(this);
        return networkEngine.createService(RobotService.class);
    }

    @Override
    public void disconnect() throws IOException, InterruptedException {
        RobotEventListener.publish().beforeDisconnect(this);
        if (getNetworkEngine() != null) {
            getNetworkEngine().close();
            this.networkEngine = null;
        }
        RobotEventListener.publish().afterDisconnect(this);
    }

    @Override
    public void dispose() {
        if (getNetworkEngine() != null) {
            try {
                getNetworkEngine().close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException ignored) {}
            this.networkEngine = null;
        }
    }
}
