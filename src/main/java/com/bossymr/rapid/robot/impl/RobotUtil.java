package com.bossymr.rapid.robot.impl;

import com.bossymr.rapid.language.RapidFileType;
import com.bossymr.rapid.language.symbol.virtual.VirtualSymbol;
import com.bossymr.rapid.robot.PersistentRobotState;
import com.bossymr.rapid.robot.PersistentRobotState.StorageSymbolState;
import com.bossymr.rapid.robot.ResponseStatusException;
import com.bossymr.rapid.robot.network.RobotService;
import com.bossymr.rapid.robot.network.SymbolQueryBuilder;
import com.bossymr.rapid.robot.network.SymbolState;
import com.bossymr.rapid.robot.network.SymbolType;
import com.bossymr.rapid.robot.network.query.Query;
import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.credentialStore.Credentials;
import com.intellij.ide.passwordSafe.PasswordSafe;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

import static com.intellij.credentialStore.CredentialAttributesKt.generateServiceName;

public final class RobotUtil {

    private RobotUtil() {
    }

    private static @NotNull CredentialAttributes createCredentialAttributes(String key) {
        return new CredentialAttributes(generateServiceName("Robot", key));
    }

    public static @Nullable Credentials getCredentials(@NotNull URI path) {
        CredentialAttributes credentialAttributes = createCredentialAttributes(path.toString());
        return PasswordSafe.getInstance().get(credentialAttributes);
    }

    public static void setCredentials(@NotNull URI path, @NotNull Credentials credentials) {
        CredentialAttributes credentialAttributes = createCredentialAttributes(path.toString());
        PasswordSafe.getInstance().set(credentialAttributes, credentials);
    }

    public static void reload() {
        ApplicationManager.getApplication().invokeLater(() -> {
            Project[] projects = ProjectManager.getInstance().getOpenProjects();
            for (Project project : projects) {
                Collection<VirtualFile> virtualFiles = FileTypeIndex.getFiles(RapidFileType.INSTANCE, GlobalSearchScope.projectScope(project));
                PsiDocumentManager.getInstance(project).reparseFiles(virtualFiles, true);
            }
        });
    }

    public static @NotNull Map<String, VirtualSymbol> getSymbols(@NotNull PersistentRobotState robotState) {
        return new RobotSymbolFactory(robotState).getSymbols();
    }

    public static @NotNull VirtualSymbol getSymbol(@NotNull StorageSymbolState symbolState) {
        List<SymbolState> symbolStates = List.of(PersistentRobotState.getSymbolState(symbolState));
        Map<String, VirtualSymbol> symbols = new RobotSymbolFactory(symbolStates).getSymbols();
        List<VirtualSymbol> virtualSymbols = new ArrayList<>(symbols.values());
        return virtualSymbols.get(0);
    }

    public static @NotNull PersistentRobotState getRobotState(@NotNull RobotService service) {
        PersistentRobotState robotState = new PersistentRobotState();
        Map<String, String> query = new SymbolQueryBuilder()
                .setRecursive(true)
                .setSymbolType(SymbolType.ANY)
                .build();
        CompletableFuture.allOf(
                service.getControllerService().getIdentity().sendAsync()
                        .thenAcceptAsync(identity -> {
                            robotState.name = identity.getName();
                            URI self = identity.getLink("self");
                            robotState.path = self.getScheme() + "://" + self.getHost() + ":" + self.getPort();
                        }),
                service.getRobotWareService().getRapidService().findSymbols(query).sendAsync()
                        .thenApplyAsync(symbols -> {
                            robotState.symbols = symbols.stream()
                                    .map(RobotUtil::getSymbolState)
                                    .collect(Collectors.toSet());
                            return symbols;
                        }).thenComposeAsync(symbols -> {
                            Map<String, Set<String>> states = new HashMap<>();
                            for (SymbolState symbol : symbols) {
                                String address = symbol.getTitle().substring(0, symbol.getTitle().lastIndexOf('/'));
                                states.computeIfAbsent(address, (value) -> new HashSet<>());
                                states.get(address).add(symbol.getTitle().substring(symbol.getTitle().lastIndexOf('/') + 1));
                            }
                            List<CompletableFuture<?>> completableFutures = new ArrayList<>();
                            for (Map.Entry<String, Set<String>> entry : states.entrySet()) {
                                if (entry.getKey().equals("RAPID")) continue;
                                String name = entry.getKey().substring(entry.getKey().lastIndexOf('/') + 1);
                                if (!states.get("RAPID").contains(name)) {
                                    Query<SymbolState> symbol = service.getRobotWareService().getRapidService().findSymbol(entry.getKey());
                                    CompletableFuture<Void> completableFuture = symbol.sendAsync()
                                            .exceptionally((exception) -> {
                                                if (exception.getCause() instanceof ResponseStatusException responseStatusException) {
                                                    if (responseStatusException.getStatusCode() == 400) {
                                                        return null;
                                                    }
                                                }
                                                if (exception instanceof RuntimeException runtimeException) {
                                                    throw runtimeException;
                                                }
                                                throw new CompletionException(exception);
                                            }).thenAcceptAsync((response) -> {
                                                if (response != null) {
                                                    StorageSymbolState storageSymbolState = RobotUtil.getSymbolState(response);
                                                    robotState.symbols.add(storageSymbolState);
                                                }
                                            });
                                    completableFutures.add(completableFuture);
                                }
                            }
                            return CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[0]));
                        })
        ).join();
        return robotState;
    }

    public static @NotNull StorageSymbolState getSymbolState(@NotNull SymbolState symbol) {
        StorageSymbolState symbolState = new StorageSymbolState();
        symbolState.title = symbol.getTitle();
        symbolState.type = symbol.getType();
        symbolState.fields = symbol.getFields();
        symbolState.links = new HashMap<>();
        for (Map.Entry<String, URI> entry : symbol.getLinks().entrySet()) {
            symbolState.links.put(entry.getKey(), entry.getValue().toString());
        }
        return symbolState;
    }

}
