package com.bossymr.rapid.robot.impl;

import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.language.RapidFileType;
import com.bossymr.rapid.language.symbol.virtual.VirtualSymbol;
import com.bossymr.rapid.robot.RemoteService;
import com.bossymr.rapid.robot.ResponseStatusException;
import com.bossymr.rapid.robot.Robot;
import com.bossymr.rapid.robot.RobotState;
import com.bossymr.rapid.robot.network.RobotService;
import com.bossymr.rapid.robot.network.Symbol;
import com.bossymr.rapid.robot.network.SymbolQueryBuilder;
import com.bossymr.rapid.robot.network.SymbolType;
import com.bossymr.rapid.robot.network.query.Query;
import com.bossymr.rapid.robot.ui.RobotConnectView;
import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.credentialStore.Credentials;
import com.intellij.ide.passwordSafe.PasswordSafe;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnActionEvent;
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

    public static boolean isConnected(@Nullable Project project) {
        if (project != null) {
            RemoteService service = RemoteService.getInstance();
            Robot robot = service.getRobot();
            if (robot != null) {
                return robot.getRobotService() != null;
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

    public static @NotNull Map<String, VirtualSymbol> getSymbols(@NotNull RobotState robotState) {
        return new RobotSymbolFactory(robotState).getSymbols();
    }

    public static @NotNull VirtualSymbol getSymbol(@NotNull Symbol symbol) {
        List<Symbol> symbolStates = List.of(symbol);
        Map<String, VirtualSymbol> symbols = new RobotSymbolFactory(symbolStates).getSymbols();
        List<VirtualSymbol> virtualSymbols = new ArrayList<>(symbols.values());
        return virtualSymbols.get(0);
    }

    public static @NotNull RobotState getRobotState(@NotNull RobotService service) throws IOException, InterruptedException {
        RobotState robotState = new RobotState();
        Map<String, String> query = new SymbolQueryBuilder()
                .setRecursive(true)
                .setSymbolType(SymbolType.ANY)
                .build();
        try {
            allOfExceptionally(
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
                                for (Symbol symbol : symbols) {
                                    String address = symbol.getTitle().substring(0, symbol.getTitle().lastIndexOf('/'));
                                    states.computeIfAbsent(address, (value) -> new HashSet<>());
                                    states.get(address).add(symbol.getTitle().substring(symbol.getTitle().lastIndexOf('/') + 1));
                                }
                                List<CompletableFuture<?>> completableFutures = new ArrayList<>();
                                for (Map.Entry<String, Set<String>> entry : states.entrySet()) {
                                    if (entry.getKey().equals("RAPID")) continue;
                                    String name = entry.getKey().substring(entry.getKey().lastIndexOf('/') + 1);
                                    if (!states.get("RAPID").contains(name)) {
                                        Query<Symbol> symbol = service.getRobotWareService().getRapidService().findSymbol(entry.getKey());
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
                                                        RobotState.SymbolState storageSymbolState = RobotUtil.getSymbolState(response);
                                                        robotState.symbols.add(storageSymbolState);
                                                    }
                                                });
                                        completableFutures.add(completableFuture);
                                    }
                                }
                                return CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[0]));
                            })
            ).join();
        } catch (CompletionException e) {
            if (e.getCause() instanceof IOException) {
                throw (IOException) e.getCause();
            }
            if (e.getCause() instanceof InterruptedException)
                throw (InterruptedException) e.getCause();
        }
        return robotState;
    }

    @SafeVarargs
    public static @NotNull CompletableFuture<Void> allOfExceptionally(CompletableFuture<Void> @NotNull ... completableFutures) {
        CompletableFuture<Void> collection = CompletableFuture.allOf(completableFutures);
        for (CompletableFuture<Void> completableFuture : completableFutures) {
            completableFuture.exceptionally(throwable -> {
                // The collection of completable futures should fail as soon as any of its components fails.
                if (collection.isCompletedExceptionally()) return null;
                collection.completeExceptionally(throwable);
                for (CompletableFuture<Void> future : completableFutures) {
                    // In addition, all other completable futures are canceled as soon as any of its components fail.
                    future.cancel(true);
                }
                return null;
            });
        }
        return collection;
    }

    public static @NotNull RobotState.SymbolState getSymbolState(@NotNull Symbol symbol) {
        RobotState.SymbolState symbolState = new RobotState.SymbolState();
        symbolState.title = symbol.getTitle();
        symbolState.type = symbol.getType();
        symbolState.fields = symbol.getFields();
        symbolState.links = new HashMap<>();
        for (Map.Entry<String, URI> entry : symbol.getLinks().entrySet()) {
            symbolState.links.put(entry.getKey(), entry.getValue().toString());
        }
        return symbolState;
    }

    public static void showNotification(@Nullable Project project, @NotNull URI path) {
        NotificationGroupManager.getInstance()
                .getNotificationGroup("Robot Connect Error")
                .createNotification(RapidBundle.message("notification.title.robot.connect.error", path), NotificationType.ERROR)
                .setSubtitle(RapidBundle.message("notification.subtitle.robot.connect.error"))
                .addAction(new NotificationAction(RapidBundle.message("notification.action.retry.connect")) {
                    @Override
                    public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
                        assert e.getProject() != null;
                        RobotConnectView connectView = new RobotConnectView(e.getProject(), path);
                        connectView.show();
                    }
                })
                .notify(project);
    }

}
