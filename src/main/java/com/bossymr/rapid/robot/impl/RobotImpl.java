package com.bossymr.rapid.robot.impl;

import com.bossymr.rapid.language.symbol.RapidTask;
import com.bossymr.rapid.language.symbol.virtual.VirtualSymbol;
import com.bossymr.rapid.robot.*;
import com.bossymr.rapid.robot.network.RobotService;
import com.bossymr.rapid.robot.network.SymbolState;
import com.intellij.credentialStore.Credentials;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RobotImpl implements Robot {

    private @NotNull PersistentRobotState robotState;
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
        tasks = RobotUtil.download(robotService);
        symbols = RobotUtil.getSymbols(robotState);
        RobotUtil.reload();
        RobotEventListener.publish().afterConnect(this);
    }

    /**
     * Creates a new {@code Robot} from a persisted robot state, which is not immediately connected.
     *
     * @param robotState the persisted robot state.
     */
    public RobotImpl(@NotNull PersistentRobotState robotState) {
        this.robotState = robotState;
        RemoteService.getInstance().setRobotState(robotState);
        symbols = RobotUtil.getSymbols(robotState);
        tasks = RobotUtil.download();
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
                SymbolState symbolState;
                try {
                    symbolState = robotService.getRobotWareService().getRapidService().findSymbol("RAPID" + "/" + name).send();
                } catch (ResponseStatusException e) {
                    if (e.getStatusCode() == 400) {
                        symbolState = null;
                    } else {
                        throw e;
                    }
                }
                if (symbolState != null) {
                    PersistentRobotState.StorageSymbolState storageSymbolState = RobotUtil.getSymbolState(symbolState);
                    VirtualSymbol virtualSymbol = RobotUtil.getSymbol(storageSymbolState);
                    symbols.put(virtualSymbol.getName(), virtualSymbol);
                    robotState.symbols.add(storageSymbolState);
                    RobotEventListener.publish().onSymbol(this, virtualSymbol);
                } else {
                    robotState.cache.add(name);
                }
            }
        }
        return null;
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
            RobotEventListener.publish().beforeRefresh(this);
            robotService = RobotService.connect(path, credentials);
            robotState = RobotUtil.getRobotState(robotService);
            RemoteService.getInstance().setRobotState(robotState);
            tasks = RobotUtil.download(robotService);
            symbols = RobotUtil.getSymbols(robotState);
            RobotUtil.reload();
            RobotEventListener.publish().afterRefresh(this);
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
        tasks = RobotUtil.download(robotService);
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
}
