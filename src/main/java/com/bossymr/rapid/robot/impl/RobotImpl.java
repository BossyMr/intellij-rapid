package com.bossymr.rapid.robot.impl;

import com.bossymr.rapid.language.symbol.virtual.VirtualSymbol;
import com.bossymr.rapid.robot.Robot;
import com.bossymr.rapid.robot.RobotEventListener;
import com.bossymr.rapid.robot.RobotService;
import com.bossymr.rapid.robot.RobotState;
import com.bossymr.rapid.robot.RobotState.SymbolState;
import com.bossymr.rapid.robot.network.Controller;
import com.intellij.credentialStore.Credentials;
import com.intellij.openapi.application.ApplicationManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RobotImpl implements Robot {

    private RobotState robotState;
    private Controller controller;

    private Map<String, VirtualSymbol> symbols;

    public RobotImpl(@NotNull Controller controller) throws IOException {
        setRobotState(RobotUtil.getRobotState(controller));
        this.controller = controller;
        this.symbols = RobotUtil.getSymbols(robotState);
        RobotUtil.reload();
    }

    public RobotImpl(@NotNull RobotState robotState) {
        setRobotState(robotState);
        this.symbols = RobotUtil.getSymbols(robotState);
        RobotUtil.reload();
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
        return new HashSet<>(symbols.values());
    }

    @Override
    public @Nullable VirtualSymbol getSymbol(@NotNull String name) throws IOException {
        if (symbols.containsKey(name)) {
            return symbols.get(name);
        }
        if (robotState.cache.contains(name)) {
            // The connected robot does not contain the specified symbol.
            return null;
        }
        // Unfortunately, the API to fetch all symbols from the connected robot is not complete.
        // As a result, each unresolved symbol is attempted to be resolved automatically.
        if (controller != null) {
            // Attempt to fetch symbol from the connected robot.
            SymbolState symbolState = controller.getSymbol(name);
            if (symbolState != null) {
                // The connected robot does contain the specified symbol.
                robotState.symbols.add(symbolState);
                VirtualSymbol symbol = RobotUtil.getSymbol(symbolState);
                symbols.put(name, symbol);
                RobotEventListener.publish().onSymbol(this, symbol);
                return symbol;
            } else {
                // The connected robot does not contain the specified symbol.
                robotState.cache.add(name);
            }
            return null;
        }
        return null;
    }

    @Override
    public @Nullable Controller getController() {
        return controller;
    }

    @Override
    public void reconnect() throws IOException {
        RobotEventListener.publish().beforeRefresh(this);
        URI path = URI.create(robotState.path);
        Credentials credentials = RobotUtil.getCredentials(path);
        if (credentials == null) {
            credentials = new Credentials("", "");
        }
        controller = Controller.connect(path, credentials);
        setRobotState(RobotUtil.getRobotState(controller));
        symbols = RobotUtil.getSymbols(robotState);
        RobotUtil.reload();
        RobotEventListener.publish().afterRefresh(this);
    }

    @Override
    public void reconnect(@NotNull Credentials credentials) throws IOException {
        RobotEventListener.publish().beforeRefresh(this);
        URI path = URI.create(robotState.path);
        controller = Controller.connect(path, credentials);
        setRobotState(RobotUtil.getRobotState(controller));
        symbols = RobotUtil.getSymbols(robotState);
        RobotUtil.reload();
        RobotEventListener.publish().afterRefresh(this);
    }

    public void setRobotState(RobotState robotState) {
        if (robotState.path == null) throw new IllegalArgumentException();
        if (robotState.name == null) throw new IllegalArgumentException();
        this.robotState = robotState;
        RobotService.getInstance().setRobotState(robotState);
    }

    @Override
    public void disconnect() {
        if (!ApplicationManager.getApplication().isDisposed()) {
            RobotEventListener.publish().beforeDisconnect(this);
        }
        controller = null;
        if (!ApplicationManager.getApplication().isDisposed()) {
            RobotEventListener.publish().afterDisconnect(this);
        }
    }

    @Override
    public boolean isConnected() {
        return controller != null;
    }
}
