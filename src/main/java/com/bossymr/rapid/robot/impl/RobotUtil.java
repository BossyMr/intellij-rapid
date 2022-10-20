package com.bossymr.rapid.robot.impl;

import com.bossymr.rapid.language.psi.RapidSymbol;
import com.bossymr.rapid.robot.network.controller.Controller;
import com.bossymr.rapid.robot.network.controller.rapid.Rapid;
import com.bossymr.rapid.robot.network.controller.rapid.SymbolEntity;
import com.bossymr.rapid.robot.network.controller.rapid.SymbolSearchQuery;
import com.bossymr.rapid.robot.network.controller.rapid.SymbolType;
import com.bossymr.rapid.robot.state.RobotState;
import com.bossymr.rapid.robot.state.SymbolState;
import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.credentialStore.CredentialAttributesKt;
import com.intellij.credentialStore.Credentials;
import com.intellij.ide.passwordSafe.PasswordSafe;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URI;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public final class RobotUtil {

    private RobotUtil() {}

    public static @NotNull String getName(@NotNull Controller controller) throws IOException {
        try {
            return controller.getIdentity().get().name();
        } catch (InterruptedException | ExecutionException e) {
            throw new IOException(e);
        }
    }

    private static @NotNull CredentialAttributes createCredentialAttributes(@NotNull URI path) {
        return new CredentialAttributes(CredentialAttributesKt.generateServiceName("RAPID", path.toString()));
    }

    public static @Nullable Credentials getCredentials(@NotNull URI path) {
        CredentialAttributes credentialAttributes = createCredentialAttributes(path);
        return PasswordSafe.getInstance().get(credentialAttributes);
    }

    public static void setCredentials(@NotNull URI path, @NotNull Credentials credentials) {
        CredentialAttributes credentialAttributes = createCredentialAttributes(path);
        PasswordSafe.getInstance().set(credentialAttributes, credentials);
    }

    public static @NotNull Controller getController(@NotNull URI path) throws IOException {
        Credentials credentials = getCredentials(path);
        return createController(path, credentials != null ? credentials : new Credentials("", ""));
    }

    public static @NotNull Controller getController(@NotNull URI path, @NotNull Credentials credentials) throws IOException {
        setCredentials(path, credentials);
        return createController(path, credentials);
    }

    private static @NotNull Controller createController(@NotNull URI path, @NotNull Credentials credentials) throws IOException {
        String username = credentials.getUserName() != null ? credentials.getUserName() : "";
        String password = credentials.getPasswordAsString() != null ? credentials.getPasswordAsString() : "";
        return Controller.connect(path, username, password);
    }

    public static @NotNull Map<String, RapidSymbol> getSymbols(@NotNull PsiManager manager, @NotNull RobotState robotState) {
        RobotSymbolFactory factory = new RobotSymbolFactory(manager, robotState);
        return factory.getSymbols();
    }

    public static @NotNull RobotState getState(@NotNull Controller controller) throws IOException {
        RobotState robotState = new RobotState();
        try {
            robotState.name = controller.getIdentity().get().name();
        } catch (InterruptedException | ExecutionException e) {
            throw new IOException(e);
        }
        robotState.path = controller.getPath().toString();
        robotState.symbols = getSymbols(controller.getRapid());
        return robotState;
    }

    private static @NotNull List<SymbolState> getSymbols(@NotNull Rapid rapid) throws IOException {
        SymbolSearchQuery query = SymbolSearchQuery.newBuilder()
                .setSymbolType(SymbolType.ANY)
                .build();
        List<SymbolEntity> entities;
        try {
            entities = rapid.getSymbols(query).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new IOException(e);
        }
        EnumSet<SymbolType> symbolTypes = EnumSet.complementOf(EnumSet.of(SymbolType.MODULE, SymbolType.TASK, SymbolType.ANY, SymbolType.UNDEFINED));
        return entities.stream()
                .filter(entity -> symbolTypes.contains(entity.symbolType()))
                .map(entity -> {
                    SymbolState state = new SymbolState();
                    state.name = entity.name();
                    state.path = entity.symbol();
                    state.type = entity.symbolType();
                    state.mode = entity.mode();
                    state.isRequired = entity.required();
                    state.dataType = entity.dataType();
                    if (entity.componentLength() != null) {
                        state.length = entity.componentLength();
                    } else if (entity.parameterLength() != null) {
                        state.length = entity.parameterLength();
                    } else {
                        state.length = 0;
                    }
                    if (entity.componentIndex() != null) {
                        state.index = entity.componentIndex() - 1;
                    } else if (entity.parameterIndex() != null) {
                        state.index = entity.parameterIndex() - 1;
                    } else {
                        state.index = 0;
                    }
                    state.dimension = entity.dimensions();
                    state.size = entity.length();
                    return state;
                }).toList();
    }
}
