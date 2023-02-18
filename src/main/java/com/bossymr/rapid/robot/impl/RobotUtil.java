package com.bossymr.rapid.robot.impl;

import com.bossymr.network.NetworkCall;
import com.bossymr.network.ResponseStatusException;
import com.bossymr.network.client.NetworkEngine;
import com.bossymr.network.client.security.Credentials;
import com.bossymr.rapid.language.RapidFileType;
import com.bossymr.rapid.language.symbol.virtual.VirtualSymbol;
import com.bossymr.rapid.robot.RemoteRobotService;
import com.bossymr.rapid.robot.Robot;
import com.bossymr.rapid.robot.RobotState;
import com.bossymr.rapid.robot.network.RobotService;
import com.bossymr.rapid.robot.network.robotware.rapid.symbol.SymbolQuery;
import com.bossymr.rapid.robot.network.robotware.rapid.symbol.SymbolState;
import com.bossymr.rapid.robot.network.robotware.rapid.symbol.SymbolType;
import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.ide.passwordSafe.PasswordSafe;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

import static com.intellij.credentialStore.CredentialAttributesKt.generateServiceName;

public final class RobotUtil {

    private RobotUtil() {
        throw new AssertionError();
    }

    private static @NotNull CredentialAttributes createCredentialAttributes(String key) {
        return new CredentialAttributes(generateServiceName("Robot", key));
    }

    public static @Nullable Credentials getCredentials(@NotNull URI path) {
        CredentialAttributes credentialAttributes = createCredentialAttributes(path.toString());
        com.intellij.credentialStore.Credentials credentials = PasswordSafe.getInstance().get(credentialAttributes);
        if (credentials == null) {
            return null;
        }
        assert credentials.getPassword() != null;
        assert credentials.getUserName() != null;
        return new Credentials(credentials.getUserName(), credentials.getPassword().toCharArray());
    }

    public static void setCredentials(@NotNull URI path, @NotNull String username, char @NotNull [] password) {
        CredentialAttributes credentialAttributes = createCredentialAttributes(path.toString());
        PasswordSafe.getInstance().set(credentialAttributes, new com.intellij.credentialStore.Credentials(username, password));
    }

    public static void reload() {
        ApplicationManager.getApplication().invokeLater(() -> {
            Project[] projects = ProjectManager.getInstance().getOpenProjects();
            for (Project project : projects) {
                Collection<VirtualFile> virtualFiles = FileTypeIndex.getFiles(RapidFileType.getInstance(), GlobalSearchScope.projectScope(project));
                PsiDocumentManager.getInstance(project).reparseFiles(virtualFiles, true);
            }
        });
    }

    public static boolean isConnected(@Nullable Project project) {
        if (project != null) {
            RemoteRobotService service = RemoteRobotService.getInstance();
            Robot robot = service.getRobot();
            if (robot != null) {
                return robot.isConnected();
            }
        }
        return false;
    }

    public static void remove() throws IOException {
        String homePath = PathManager.getSystemPath();
        Path robotPath = Path.of(homePath, "robot");
        File file = robotPath.toFile();
        if (file.exists()) FileUtil.delete(robotPath);
    }

    public static @NotNull Map<String, VirtualSymbol> getSymbols(@Nullable NetworkEngine networkEngine, @NotNull RobotState robotState) {
        return RapidSymbolConverter.getSymbols(robotState.getSymbols(networkEngine));
    }

    public static @NotNull VirtualSymbol getSymbol(@NotNull SymbolState symbolState) {
        List<SymbolState> symbolStates = List.of(symbolState);
        Map<String, VirtualSymbol> symbols = RapidSymbolConverter.getSymbols(symbolStates);
        List<VirtualSymbol> virtualSymbols = new ArrayList<>(symbols.values());
        return virtualSymbols.get(0);
    }

    public static @NotNull RobotState getRobotState(@NotNull NetworkEngine networkEngine) throws IOException, InterruptedException {
        RobotService service = networkEngine.createService(RobotService.class);
        RobotState robotState = new RobotState();
        SymbolQuery query = new SymbolQuery()
                .setRecursive(true)
                .setSymbolType(SymbolType.ANY);
        try {
            CompletableFuture.allOf(
                    service.getControllerService().getIdentity().sendAsync()
                            .thenAcceptAsync(identity -> {
                                robotState.name = identity.getName();
                                URI self = identity.getLink("self");
                                robotState.path = self.getScheme() + "://" + self.getHost() + ":" + self.getPort();
                            }),
                    service.getRobotWareService().getRapidService().findSymbols(query).sendAsync()
                            .thenApplyAsync(symbols -> {
                                robotState.symbolStates = symbols.stream()
                                        .map(RobotUtil::getSymbolState)
                                        .collect(Collectors.toSet());
                                return symbols;
                            }).thenComposeAsync(symbols -> {
                                Map<String, Set<String>> states = new HashMap<>();
                                for (SymbolState symbolState : symbols) {
                                    String address = symbolState.getTitle().substring(0, symbolState.getTitle().lastIndexOf('/'));
                                    states.computeIfAbsent(address, (value) -> new HashSet<>());
                                    states.get(address).add(symbolState.getTitle().substring(symbolState.getTitle().lastIndexOf('/') + 1));
                                }
                                List<CompletableFuture<Void>> completableFutures = new ArrayList<>();
                                for (Map.Entry<String, Set<String>> entry : states.entrySet()) {
                                    if (entry.getKey().equals("RAPID")) continue;
                                    String name = entry.getKey().substring(entry.getKey().lastIndexOf('/') + 1);
                                    if (!states.get("RAPID").contains(name)) {
                                        NetworkCall<SymbolState> symbol = service.getRobotWareService().getRapidService().findSymbol(entry.getKey());
                                        CompletableFuture<Void> completableFuture = symbol.sendAsync()
                                                .exceptionally((exception) -> {
                                                    if (exception.getCause() instanceof ResponseStatusException responseStatusException) {
                                                        if (responseStatusException.getResponse().statusCode() == 400) {
                                                            return null;
                                                        }
                                                    }
                                                    if (exception instanceof RuntimeException runtimeException) {
                                                        throw runtimeException;
                                                    }
                                                    throw new CompletionException(exception);
                                                }).thenAcceptAsync((response) -> {
                                                    if (response != null) {
                                                        RobotState.SymbolState storageSymbolState = RobotUtil.getSymbolState(response);
                                                        robotState.symbolStates.add(storageSymbolState);
                                                    }
                                                });
                                        completableFutures.add(completableFuture);
                                    }
                                }
                                return CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[0]));
                            })
            ).join();
            return robotState;
        } catch (CompletionException e) {
            if (e.getCause() instanceof IOException) {
                throw (IOException) e.getCause();
            }
            if (e.getCause() instanceof InterruptedException) {
                throw (InterruptedException) e.getCause();
            }
            throw e;
        }
    }

    public static @NotNull RobotState.SymbolState getSymbolState(@NotNull SymbolState symbol) {
        RobotState.SymbolState symbolState = new RobotState.SymbolState();
        symbolState.title = symbol.getTitle().toLowerCase();
        symbolState.type = symbol.getType();
        symbolState.fields = symbol.getFields();
        symbolState.links = new HashMap<>();
        for (Map.Entry<String, URI> entry : symbol.getLinks().entrySet()) {
            symbolState.links.put(entry.getKey(), entry.getValue().toString());
        }
        return symbolState;
    }

}
