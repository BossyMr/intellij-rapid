package com.bossymr.rapid.robot.impl;

import com.bossymr.rapid.language.symbol.virtual.VirtualSymbol;
import com.bossymr.rapid.robot.*;
import com.bossymr.rapid.robot.network.RobotService;
import com.bossymr.rapid.robot.network.SymbolState;
import com.intellij.credentialStore.Credentials;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class RobotImpl implements Robot {

    private @NotNull PersistentRobotState robotState;
    private @Nullable RobotService robotService;

    private @NotNull Map<String, VirtualSymbol> symbols;

    /**
     * Creates a new {@code Robot} which is connected to the remote robot through the specified service.
     *
     * @param robotService the robot service.
     */
    public RobotImpl(@NotNull RobotService robotService) {
        this.robotService = robotService;
        robotState = RobotUtil.getRobotState(robotService);
        RemoteService.getInstance().setRobotState(robotState);
        RobotUtil.reload();
        symbols = RobotUtil.getSymbols(robotState);
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
    public void reconnect() throws IOException {
        RobotEventListener.publish().beforeRefresh(this);
        URI path = URI.create(robotState.path);
        robotService = RobotService.connect(path, Objects.requireNonNull(RobotUtil.getCredentials(path)));
        robotState = RobotUtil.getRobotState(robotService);
        RemoteService.getInstance().setRobotState(robotState);
        RobotUtil.reload();
        symbols = RobotUtil.getSymbols(robotState);
        RobotEventListener.publish().afterRefresh(this);
    }

    @Override
    public void reconnect(@NotNull Credentials credentials) throws IOException {
        RobotEventListener.publish().beforeRefresh(this);
        URI path = URI.create(robotState.path);
        RobotUtil.setCredentials(path, credentials);
        robotService = RobotService.connect(path, credentials);
        robotState = RobotUtil.getRobotState(robotService);
        RemoteService.getInstance().setRobotState(robotState);
        RobotUtil.reload();
        symbols = RobotUtil.getSymbols(robotState);
        RobotEventListener.publish().afterRefresh(this);
    }

    @Override
    public void disconnect() throws IOException {
        RobotEventListener.publish().beforeDisconnect(this);
        if (getRobotService() != null) {
            getRobotService().getNetworkClient().close();
        }
        RobotEventListener.publish().afterDisconnect(this);
    }
}
