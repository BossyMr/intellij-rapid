package com.bossymr.rapid.robot.impl;

import com.bossymr.rapid.language.RapidFileType;
import com.bossymr.rapid.language.psi.RapidSymbol;
import com.bossymr.rapid.robot.Robot;
import com.bossymr.rapid.robot.RobotService;
import com.bossymr.rapid.robot.network.controller.Controller;
import com.bossymr.rapid.robot.state.RobotState;
import com.intellij.credentialStore.Credentials;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class RobotImpl implements Robot {

    private static final Logger LOG = Logger.getInstance(Robot.class);

    private final Project project;

    private final URI path;

    private String name;
    private Map<String, RapidSymbol> symbols;
    private Controller controller;

    public RobotImpl(@NotNull Project project, @NotNull RobotState robotState) {
        this(project, robotState, null);
    }

    public RobotImpl(@NotNull Project project, @NotNull RobotState robotState, @Nullable Controller controller) {
        this.project = project;
        this.path = URI.create(robotState.path);
        this.controller = controller;
        this.symbols = RobotUtil.getSymbols(project, robotState);
        this.name = robotState.name;
        project.getMessageBus().syncPublisher(RobotService.TOPIC).onRefresh(this);
    }

    private void build() throws IOException {
        controller = RobotUtil.getController(path);
        RobotState robotState = RobotUtil.getState(controller);
        symbols = RobotUtil.getSymbols(project, robotState);
        name = robotState.name;
    }

    @Override
    public @NotNull String getName() {
        return name;
    }

    @Override
    public @NotNull URI getPath() {
        return path;
    }

    public @NotNull Set<RapidSymbol> getSymbols() {
        return Set.copyOf(symbols.values());
    }

    public @NotNull Optional<RapidSymbol> getSymbol(@NotNull String name) {
        if (symbols.containsKey(name)) {
            return Optional.of(symbols.get(name));
        }
        if (isConnected()) {
            try {
                Optional<RapidSymbol> optional = RobotUtil.getSymbol(project, controller, name);
                if (optional.isPresent()) {
                    RapidSymbol symbol = optional.get();
                    symbols.put(symbol.getName(), symbol);
                }
                return optional;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean isConnected() {
        return controller != null;
    }

    @Override
    public void reconnect() throws IOException {
        LOG.debug("Reconnecting to robot: " + path);
        controller = RobotUtil.getController(path);
        build();
        ApplicationManager.getApplication().invokeLater(() -> WriteAction.run(() -> PsiDocumentManager.getInstance(project).reparseFiles(FileTypeIndex.getFiles(RapidFileType.INSTANCE, GlobalSearchScope.projectScope(project)), true)));
        project.getMessageBus().syncPublisher(RobotService.TOPIC).onRefresh(this);
    }

    @Override
    public void reconnect(@NotNull Credentials credentials) throws IOException {
        LOG.debug("Reconnecting to robot: " + path);
        controller = RobotUtil.getController(path, credentials);
        build();
        ApplicationManager.getApplication().invokeLater(() -> WriteAction.run(() -> PsiDocumentManager.getInstance(project).reparseFiles(FileTypeIndex.getFiles(RapidFileType.INSTANCE, GlobalSearchScope.projectScope(project)), true)));
        project.getMessageBus().syncPublisher(RobotService.TOPIC).onRefresh(this);
    }

    @Override
    public void disconnect() {
        LOG.debug("Disconnect to robot: " + path);
        controller = null;
        project.getMessageBus().syncPublisher(RobotService.TOPIC).onDisconnect(this);
    }
}
