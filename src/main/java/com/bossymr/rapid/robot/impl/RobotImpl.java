package com.bossymr.rapid.robot.impl;

import com.bossymr.network.ResponseStatusException;
import com.bossymr.network.client.DelegatingNetworkEngine;
import com.bossymr.network.client.NetworkEngine;
import com.bossymr.network.client.security.Credentials;
import com.bossymr.rapid.language.RapidFileType;
import com.bossymr.rapid.language.symbol.RapidTask;
import com.bossymr.rapid.language.symbol.virtual.VirtualSymbol;
import com.bossymr.rapid.robot.RemoteRobotService;
import com.bossymr.rapid.robot.Robot;
import com.bossymr.rapid.robot.RobotEventListener;
import com.bossymr.rapid.robot.RobotState;
import com.bossymr.rapid.robot.network.Module;
import com.bossymr.rapid.robot.network.*;
import com.bossymr.rapid.robot.network.robotware.rapid.symbol.SymbolState;
import com.intellij.openapi.Disposable;
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
import java.util.stream.Collectors;

public class RobotImpl implements Robot, Disposable {

    private @NotNull RobotState robotState;
    private @Nullable NetworkEngine networkEngine;

    private @NotNull List<RapidTask> tasks;
    private @NotNull Map<String, VirtualSymbol> symbols;

    public RobotImpl(@NotNull URI path, @NotNull String username, char @NotNull [] password) throws IOException, InterruptedException {
        NetworkEngine engine = new NetworkEngine(path, () -> new Credentials(username, password));
        this.networkEngine = new RobotDelegatingNetworkEngine(engine);
        robotState = RobotUtil.getRobotState(networkEngine);
        RemoteRobotService.getInstance().setRobotState(robotState);
        symbols = RobotUtil.getSymbols(networkEngine, robotState);
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
        RemoteRobotService.getInstance().setRobotState(robotState);
        symbols = RobotUtil.getSymbols(null, robotState);
        tasks = new ArrayList<>();
        retrieve();
    }

    @Override
    public @NotNull String getName() {
        return robotState.name;
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
        name = name.toLowerCase();
        if (symbols.containsKey(name)) {
            return symbols.get(name);
        } else {
            if (getNetworkEngine() != null) {
                RobotService robotService = getNetworkEngine().createService(RobotService.class);
                if (robotState.cache.contains(name)) {
                    return null;
                }
                SymbolState symbolState;
                try {
                    symbolState = robotService.getRobotWareService().getRapidService().findSymbol("RAPID" + "/" + name).send();
                } catch (ResponseStatusException e) {
                    if (e.getResponse().statusCode() == 400) {
                        symbolState = null;
                    } else {
                        throw e;
                    }
                }
                if (symbolState != null) {
                    RobotState.SymbolState storageSymbolState = RobotUtil.getSymbolState(symbolState);
                    VirtualSymbol virtualSymbol = RobotUtil.getSymbol(symbolState);
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
    public void upload() throws IOException, InterruptedException {
        if (networkEngine == null) throw new IllegalStateException();
        for (RapidTask task : getTasks()) {
            Set<VirtualFile> modules = task.getFiles().stream()
                    .map(file -> LocalFileSystem.getInstance().findFileByIoFile(file))
                    .collect(Collectors.toSet());
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
        if (networkEngine == null) throw new IllegalStateException();
        try (NetworkEngine delegating = new DelegatingNetworkEngine.ShutdownOnFailure(networkEngine)) {
            RobotService robotService = delegating.createService(RobotService.class);
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
    }

    @Override
    public void download() throws IOException, InterruptedException {
        if (networkEngine == null) throw new IllegalStateException();
        try (NetworkEngine delegating = new DelegatingNetworkEngine.ShutdownOnFailure(networkEngine)) {
            RobotService robotService = delegating.createService(RobotService.class);
            List<RapidTask> rapidTasks = new ArrayList<>();
            Path defaultPath = Path.of(PathManager.getSystemPath(), "robot");
            File defaultFile = defaultPath.toFile();
            if (defaultFile.exists()) FileUtil.delete(defaultFile);
            if (!defaultFile.mkdir()) throw new IOException();
            List<Task> remoteTasks = robotService.getRobotWareService().getRapidService().getTaskService().getTasks().send();
            for (Task remoteTask : remoteTasks) {
                File taskFile = defaultPath.resolve(remoteTask.getName()).toFile();
                if (!taskFile.mkdir()) throw new IOException();
                Set<File> virtualFiles = new HashSet<>();
                RapidTask rapidTask = new RapidTaskImpl(remoteTask.getName(), taskFile, virtualFiles);
                List<ModuleInfo> moduleInfos = remoteTask.getModules().send();
                for (ModuleInfo moduleInfo : moduleInfos) {
                    Module module = moduleInfo.getModule().send();
                    module.save(module.getName(), taskFile.getPath()).send();
                    virtualFiles.add(taskFile.toPath().resolve(module.getName() + RapidFileType.DEFAULT_DOT_EXTENSION).toFile());
                }
                rapidTasks.add(rapidTask);
            }
            this.tasks = rapidTasks;
        }
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
        RobotUtil.setCredentials(path, credentials.username(), credentials.password());
        NetworkEngine engine = new NetworkEngine(path, () -> credentials);
        this.networkEngine = new RobotDelegatingNetworkEngine(engine);
        robotState = RobotUtil.getRobotState(networkEngine);
        RemoteRobotService.getInstance().setRobotState(robotState);
        symbols = RobotUtil.getSymbols(networkEngine, robotState);
        retrieve();
        RobotUtil.reload();
        RobotEventListener.publish().afterRefresh(this);
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
