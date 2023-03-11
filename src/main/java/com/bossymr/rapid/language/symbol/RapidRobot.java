package com.bossymr.rapid.language.symbol;

import com.bossymr.network.EntityModel;
import com.bossymr.network.ResponseStatusException;
import com.bossymr.network.client.DelegatingNetworkEngine;
import com.bossymr.network.client.NetworkEngine;
import com.bossymr.network.client.security.Credentials;
import com.bossymr.network.model.Model;
import com.bossymr.rapid.language.RapidFileType;
import com.bossymr.rapid.language.symbol.virtual.VirtualSymbol;
import com.bossymr.rapid.robot.RobotEventListener;
import com.bossymr.rapid.robot.impl.RapidTaskImpl;
import com.bossymr.rapid.robot.impl.RobotDelegatingNetworkEngine;
import com.bossymr.rapid.robot.impl.SymbolConverter;
import com.bossymr.rapid.robot.network.ControllerService;
import com.bossymr.rapid.robot.network.LoadProgramMode;
import com.bossymr.rapid.robot.network.robotware.mastership.CloseableMastership;
import com.bossymr.rapid.robot.network.robotware.mastership.MastershipDomain;
import com.bossymr.rapid.robot.network.robotware.mastership.MastershipService;
import com.bossymr.rapid.robot.network.robotware.mastership.MastershipType;
import com.bossymr.rapid.robot.network.robotware.rapid.RapidService;
import com.bossymr.rapid.robot.network.robotware.rapid.symbol.SymbolModel;
import com.bossymr.rapid.robot.network.robotware.rapid.symbol.SymbolQuery;
import com.bossymr.rapid.robot.network.robotware.rapid.symbol.SymbolSearchMethod;
import com.bossymr.rapid.robot.network.robotware.rapid.symbol.SymbolType;
import com.bossymr.rapid.robot.network.robotware.rapid.task.Task;
import com.bossymr.rapid.robot.network.robotware.rapid.task.TaskService;
import com.bossymr.rapid.robot.network.robotware.rapid.task.module.ModuleInfo;
import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.credentialStore.CredentialAttributesKt;
import com.intellij.credentialStore.OneTimeString;
import com.intellij.ide.passwordSafe.PasswordSafe;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.messages.Topic;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class RapidRobot {

    @Topic.AppLevel
    public static final Topic<StateListener> STATE_TOPIC = Topic.create("Robot State", StateListener.class);
    private static final Logger logger = Logger.getInstance(RapidRobot.class);

    private @NotNull State state = new State();
    private @Nullable NetworkEngine engine;
    private @NotNull Set<RapidTask> tasks;
    private @NotNull Map<String, VirtualSymbol> symbols;

    private RapidRobot(@Nullable NetworkEngine engine, @NotNull State state) {
        if (state.name == null || state.path == null || state.symbols == null || state.cache == null) {
            String symbols = "(symbols: " + (state.symbols != null ? state.symbols.size() : "null") + ")";
            String cache = "(cache: " + (state.cache != null ? state.cache.size() : "null") + ")";
            throw new IllegalArgumentException("State '" + state + "'" + symbols + " " + cache + " is invalid");
        }
        this.engine = engine;
        setState(state);
        this.symbols = SymbolConverter.getSymbols(state.symbols.stream()
                .map(symbol -> symbol.convert(SymbolModel.class, null))
                .filter(Objects::nonNull)
                .toList());
        this.tasks = getPersistedTasks();
    }

    /**
     * Create a new robot with the specified state.
     *
     * @return the robot.
     */
    public static @NotNull RapidRobot create(@NotNull State state) throws IllegalArgumentException {
        return new RapidRobot(null, state);
    }

    /**
     * Create a new robot by connecting to the specified robot.
     *
     * @param path the path to the robot.
     * @param credentials the credentials to authenticate with.
     * @return the robot.
     */
    public static @NotNull CompletableFuture<@NotNull RapidRobot> connect(@NotNull URI path, @NotNull Credentials credentials) {
        setCredentials(path, credentials);
        NetworkEngine engine = new NetworkEngine(path, () -> credentials);
        DelegatingNetworkEngine delegating = new RobotDelegatingNetworkEngine(engine);
        return getState(path, delegating)
                .thenComposeAsync(state -> {
                    RapidRobot robot = new RapidRobot(delegating, state);
                    RobotEventListener.publish().onRefresh(robot, delegating);
                    return robot.download().thenApplyAsync(unused -> robot);
                });
    }

    private static <T> @NotNull CompletableFuture<Void> combine(@NotNull Collection<? extends T> elements, @NotNull Function<T, CompletableFuture<Void>> converter) {
        return CompletableFuture.allOf(elements.stream().map(converter).filter(Objects::nonNull).toList().toArray(CompletableFuture[]::new));
    }

    private static @NotNull CompletableFuture<@NotNull State> getState(@NotNull URI path, @NotNull NetworkEngine engine) {
        State state = new State();
        state.path = path.getScheme() + "://" + path.getHost() + ":" + path.getPort();
        state.cache = new HashSet<>();
        SymbolQuery query = new SymbolQuery()
                .setRecursive(true)
                .setMethod(SymbolSearchMethod.BLOCK)
                .setBlock("RAPID")
                .setSymbolType(SymbolType.ANY);
        return CompletableFuture.allOf(
                engine.createService(ControllerService.class).getIdentity().sendAsync()
                        .thenAcceptAsync(identity -> state.name = identity.getName()),
                engine.createService(RapidService.class).findSymbols(query).sendAsync()
                        .thenApplyAsync(states -> {
                            state.symbols = states.stream()
                                    .map(Entity::convert)
                                    .collect(Collectors.toSet());
                            return states;
                        }).thenApplyAsync(symbols -> {
                            Map<String, Set<String>> states = new HashMap<>();
                            for (SymbolModel symbol : symbols) {
                                String title = symbol.getTitle();
                                int index = title.lastIndexOf('/');
                                String address = title.substring(0, index);
                                states.computeIfAbsent(address, value -> new HashSet<>());
                                states.get(address).add(title.substring(index + 1));
                            }
                            return states;
                        }).thenComposeAsync(states -> combine(states.entrySet(), entry -> {
                            if (entry.getKey().equals("RAPID")) return null;
                            String name = entry.getKey().substring(entry.getKey().lastIndexOf('/') + 1);
                            if (states.get("RAPID").contains(name)) return null;
                            return getSymbol(engine, entry.getKey())
                                    .thenAcceptAsync(response -> {
                                        if (response == null) {
                                            throw new IllegalStateException("Could not find symbol '" + entry.getKey() + "'");
                                        }
                                        Entity entity = Entity.convert(response);
                                        Objects.requireNonNull(state.symbols);
                                        state.symbols.add(entity);
                                    });
                        }))
        ).thenApplyAsync(unused -> state);
    }

    private static @NotNull CompletableFuture<@Nullable SymbolModel> getSymbol(@NotNull NetworkEngine engine, @NotNull String name) {
        return engine.createService(RapidService.class).findSymbol(name).sendAsync()
                .exceptionally(throwable -> {
                    if (throwable instanceof ResponseStatusException exception) {
                        if (exception.getResponse().statusCode() == 400) {
                            return null;
                        }
                    }
                    throw throwable instanceof RuntimeException runtimeException ? runtimeException : new CompletionException(throwable);
                });
    }

    private static @NotNull CredentialAttributes createCredentialsAttributes(@NotNull URI path) {
        return new CredentialAttributes(CredentialAttributesKt.generateServiceName("intellij-rapid", path.toString()));
    }

    private static @Nullable Credentials getCredentials(@NotNull URI path) {
        CredentialAttributes credentialsAttributes = createCredentialsAttributes(path);
        com.intellij.credentialStore.Credentials credentials = PasswordSafe.getInstance().get(credentialsAttributes);
        if (credentials == null) {
            return null;
        }
        String username = credentials.getUserName();
        OneTimeString password = credentials.getPassword();
        if (username == null || password == null) {
            return null;
        }
        return new Credentials(username, password.toCharArray());
    }

    private static void setCredentials(@NotNull URI path, @NotNull Credentials credentials) {
        CredentialAttributes credentialsAttributes = createCredentialsAttributes(path);
        PasswordSafe.getInstance().set(credentialsAttributes, new com.intellij.credentialStore.Credentials(credentials.username(), credentials.password()));
    }

    public @NotNull State getState() {
        return state;
    }

    private void setState(@NotNull State state) {
        ApplicationManager.getApplication().getMessageBus().syncPublisher(RapidRobot.STATE_TOPIC).newState(this, state);
        this.state = state;
    }

    public @NotNull String getName() {
        return state.name != null ? state.name : "";
    }

    /**
     * Checks if this robot is currently connected to the remote robot.
     *
     * @return if this robot is currently connected.
     */
    public boolean isConnected() {
        return engine != null;
    }

    /**
     * Returns the network engine used to communicate with the remote robot.
     *
     * @return the network engine, or {@code null} if this robot is not currently connected.
     */
    public @Nullable NetworkEngine getNetworkEngine() {
        return engine;
    }

    /**
     * Returns the task in this robot with the specified name.
     *
     * @param name the name of the task.
     * @return the task, or {@code null} if a task was not found with the specified name.
     */
    public @Nullable RapidTask getTask(@NotNull String name) {
        for (RapidTask task : tasks) {
            if (task.getName().equalsIgnoreCase(name)) {
                return task;
            }
        }
        return null;
    }

    /**
     * Returns all tasks in this robot.
     *
     * @return all tasks in this robot.
     */
    public @NotNull Set<RapidTask> getTasks() {
        return tasks;
    }

    /**
     * Returns the symbol in this robot with the specified name. If a symbol is found, it is persisted and returned by
     * subsequent calls to {@link #getSymbols()}.
     *
     * @param name the name of the symbol.
     * @return the symbol, or {@code null} if a symbol was not found with the specified name.
     */
    public @NotNull CompletableFuture<@Nullable VirtualSymbol> getSymbol(@NotNull String name) {
        if (symbols.containsKey(name)) {
            return CompletableFuture.completedFuture(symbols.get(name));
        }
        if (state.cache != null && state.cache.contains(name)) {
            return CompletableFuture.completedFuture(null);
        }
        if (engine == null) {
            return CompletableFuture.completedFuture(null);
        }
        return getSymbol(engine, "RAPID" + "/" + name)
                .thenApplyAsync(entity -> {
                    if (entity == null) {
                        if (state.cache == null) {
                            state.cache = new HashSet<>();
                        }
                        state.cache.add(name);
                        return null;
                    }
                    VirtualSymbol symbol = SymbolConverter.getSymbol(entity);
                    symbols.put(symbol.getName(), symbol);
                    if (state.symbols == null) {
                        state.symbols = new HashSet<>();
                    }
                    state.symbols.add(Entity.convert(entity));
                    RobotEventListener.publish().onSymbol(this, symbol);
                    return symbol;
                });
    }

    /**
     * Returns all symbols in this robot. All symbols might not be returned by this method, to search for a specific
     * name, use {@link #getSymbol(String)} which will correctly return the symbol.
     *
     * @return all symbols in this robot.
     */
    public @NotNull Set<VirtualSymbol> getSymbols() {
        return new HashSet<>(symbols.values());
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
        VirtualFile virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file);
        if (virtualFile == null) {
            throw new IOException("Failed to find directory '" + file + "'");
        }
        return virtualFile;
    }

    public @NotNull CompletableFuture<Void> download() {
        if (engine == null) {
            throw new IllegalStateException("Robot is not connected");
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
        Set<RapidTask> updated = new HashSet<>();
        return engine.createService(TaskService.class).getTasks().sendAsync()
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

    public @NotNull CompletableFuture<Void> upload() {
        LocalFileSystem localFileSystem = LocalFileSystem.getInstance();
        return combine(getTasks(), task -> {
            Set<VirtualFile> virtualFiles = task.getFiles().stream()
                    .map(localFileSystem::findFileByIoFile)
                    .collect(Collectors.toSet());
            return upload(task, virtualFiles);
        });
    }

    private @NotNull CompletableFuture<Void> refreshAsync(@NotNull Set<VirtualFile> modules) {
        List<CompletableFuture<Void>> completableFutures = new ArrayList<>();
        for (VirtualFile module : modules) {
            CompletableFuture<Void> completableFuture = new CompletableFuture<>();
            module.refresh(true, true, () -> completableFuture.complete(null));
            completableFutures.add(completableFuture);
        }
        return CompletableFuture.allOf(completableFutures.toArray(CompletableFuture[]::new));
    }

    public @NotNull CompletableFuture<Void> upload(@NotNull RapidTask task, @NotNull Set<VirtualFile> modules) {
        if (engine == null) {
            return CompletableFuture.failedFuture(new IllegalStateException("Robot is not connected"));
        }
        VirtualFile temporaryDirectory;
        try {
            temporaryDirectory = createTemporaryDirectory("upload");
        } catch (IOException e) {
            return CompletableFuture.failedFuture(e);
        }
        return refreshAsync(modules)
                .thenComposeAsync(unused -> engine.createService(TaskService.class).getTask(task.getName()).sendAsync()
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
                            return engine.createService(MastershipService.class).getDomain(MastershipType.RAPID).sendAsync()
                                    .thenComposeAsync(domain -> CloseableMastership.requestAsync(domain, () -> program.load(file.getPath(), LoadProgramMode.REPLACE).sendAsync()));
                        }).handleAsync((ignored, throwable) -> {
                            WriteAction.runAndWait(() -> FileUtil.delete(temporaryDirectory.toNioPath().toFile()));
                            if (throwable != null) {
                                logger.error(throwable);
                            }
                            return null;
                        }));
    }

    /**
     * Runs the specified {@link CompletableFuture} with the specified mastership.
     *
     * @param mastership the mastership type.
     * @param supplier the action to perform with mastership.
     * @param <T> the return type of the action.
     * @return the {@code CompletableFuture}.
     * @throws IllegalStateException if this robot is not {@link #isConnected() connected}.
     */
    public <T> @NotNull CompletableFuture<T> withMastership(@NotNull Mastership mastership, @NotNull Supplier<CompletableFuture<T>> supplier) throws IllegalStateException {
        NetworkEngine engine = getNetworkEngine();
        if (engine == null) {
            throw new IllegalStateException("Robot is not connected");
        }
        MastershipService mastershipService = engine.createService(MastershipService.class);
        MastershipType mastershipType = switch (mastership) {
            case MOTION -> MastershipType.MOTION;
            case CONFIGURATION -> MastershipType.CONFIGURATION;
            case RAPID -> MastershipType.RAPID;
        };
        return mastershipService.getDomain(mastershipType).sendAsync()
                .thenComposeAsync(domain -> {
                    if (domain.isHolding()) {
                        return supplier.get();
                    } else {
                        return withMastership(domain, supplier);
                    }
                });
    }

    private <T> @NotNull CompletableFuture<T> withMastership(@NotNull MastershipDomain domain, @NotNull Supplier<CompletableFuture<T>> supplier) {
        return domain.request().sendAsync()
                .thenComposeAsync(unused -> {
                    CompletableFuture<T> completableFuture = new CompletableFuture<>();
                    supplier.get()
                            .handleAsync((response, throwable) -> {
                                domain.release().sendAsync()
                                        .handleAsync((unused1, throwable1) -> {
                                            if (throwable != null || throwable1 != null) {
                                                completableFuture.completeExceptionally(throwable);
                                            } else {
                                                completableFuture.complete(response);
                                            }
                                            return null;
                                        });
                                return null;
                            });
                    return completableFuture;
                });
    }

    public @NotNull CompletableFuture<@NotNull NetworkEngine> reconnect() throws IllegalStateException {
        URI path = getPath();
        Credentials credentials = getCredentials(path);
        if (credentials == null) {
            throw new IllegalStateException("Credentials for '" + path + "' are not persisted");
        }
        return reconnect(path, credentials);
    }

    public @NotNull CompletableFuture<@NotNull NetworkEngine> reconnect(@NotNull Credentials credentials) {
        URI path = getPath();
        setCredentials(path, credentials);
        return reconnect(path, credentials);
    }

    private @NotNull CompletableFuture<@NotNull NetworkEngine> reconnect(@NotNull URI path, @NotNull Credentials credentials) throws IllegalArgumentException {
        NetworkEngine engine = new NetworkEngine(path, () -> credentials);
        DelegatingNetworkEngine delegating = new RobotDelegatingNetworkEngine(engine);
        return getState(path, delegating)
                .thenApplyAsync(state -> {
                    Objects.requireNonNull(state.symbols);
                    this.symbols = SymbolConverter.getSymbols(state.symbols.stream()
                            .map(symbol -> symbol.convert(SymbolModel.class, delegating))
                            .toList());
                    setState(state);
                    this.tasks = getPersistedTasks();
                    RobotEventListener.publish().onRefresh(this, delegating);
                    return this.engine = delegating;
                });
    }

    private @NotNull Path getDefaultPath() {
        return Path.of(PathManager.getSystemPath(), "robot");
    }

    private @NotNull Set<RapidTask> getPersistedTasks() {
        File file = getDefaultPath().toFile();
        Set<RapidTask> tasks = new HashSet<>();
        if (file.exists()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    File[] modules = child.listFiles();
                    if (modules != null) {
                        Set<File> files = new HashSet<>(Arrays.asList(modules));
                        RapidTask task = new RapidTaskImpl(child.getName(), child, files);
                        tasks.add(task);
                    }
                }
            }
        }
        return tasks;
    }

    public @NotNull URI getPath() throws IllegalArgumentException {
        if (state.path == null) {
            throw new IllegalStateException("State '" + state + "' is invalid");
        }
        try {
            return new URI(state.path);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("State '" + state.path + "' is invalid", e);
        }
    }

    public @NotNull CompletableFuture<Void> disconnect() {
        NetworkEngine engine = getNetworkEngine();
        if (engine == null) {
            return CompletableFuture.completedFuture(null);
        }
        this.engine = null;
        return engine.closeAsync()
                .thenRunAsync(() -> RobotEventListener.publish().onDisconnect(this));
    }

    /**
     * Represents the type of mastership which can be requested with {@link #withMastership(Mastership, Supplier)}.
     */
    public enum Mastership {
        RAPID,
        CONFIGURATION,
        MOTION
    }

    public interface StateListener {

        void newState(@NotNull RapidRobot robot, @NotNull State state);

    }

    /**
     * A model which represents the persisted state of a {@code RapidRobot2}.
     */
    public static class State {

        /**
         * The name of the robot.
         */
        public @Nullable String name;

        /**
         * The path to the robot. If the persisted value was edited manually, it might not be a valid path.
         */
        public @Nullable String path;

        /**
         * The virtual symbols on the robot.
         */
        public @Nullable Set<Entity> symbols;

        /**
         * Unfortunately, all symbols cannot be automatically retrieved. As such, each queried symbol which has not
         * already been persisted is queried manually. To avoid duplicate queries, if a symbol is not found it is added
         * a cache, and should not be queryed again.
         */
        public @Nullable Set<String> cache;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            State state = (State) o;
            return Objects.equals(name, state.name) && Objects.equals(path, state.path) && Objects.equals(symbols, state.symbols) && Objects.equals(cache, state.cache);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, path, symbols, cache);
        }

        @Override
        public String toString() {
            return "State{" +
                    "name='" + name + '\'' +
                    ", path='" + path + '\'' +
                    '}';
        }
    }

    /**
     * A model which represents the persisted state of an {@code EntityModel}.
     */
    public static class Entity implements Comparable<Entity> {

        /**
         * The title of the entity.
         */
        public @Nullable String title;

        /**
         * The type of the entity.
         */
        public @Nullable String type;

        /**
         * The field of the entity.
         */
        public @Nullable Map<String, String> fields;

        /**
         * The links of the entity.
         */
        public @Nullable Map<String, String> links;

        public Entity() {}

        private Entity(@NotNull String title, @NotNull String type, @NotNull Map<String, String> fields, @NotNull Map<String, String> links) {
            this.title = title;
            this.type = type;
            this.fields = fields;
            this.links = links;
        }

        /**
         * Converts an entity model into a persisted state.
         *
         * @param model the entity.
         * @param <T> the entity type.
         * @return the state.
         */
        public static <T extends EntityModel> @NotNull Entity convert(@NotNull T model) {
            return new Entity(model.getTitle(), model.getType(), model.getFields(), convert(model.getLinks(), k -> k, URI::toString));
        }

        private static <OK, OV, IK, IV> @NotNull Map<OK, OV> convert(@NotNull Map<IK, IV> map, @NotNull Function<IK, OK> key, @NotNull Function<IV, OV> value) {
            return map.entrySet().stream().collect(Collectors.toMap(entry -> key.apply(entry.getKey()), entry -> value.apply(entry.getValue())));
        }

        /**
         * Converts this state into an entity model of the specified type. The persisted state might be invalid, if the
         * state was edited manually.
         *
         * @param entityType the entity type.
         * @param engine the network engine, used to send requests from the created entity.
         * @param <T> the entity type.
         * @return the entity, or {@code null} if this entity is invalid.
         */
        public <T extends EntityModel> @Nullable T convert(@NotNull Class<T> entityType, @Nullable NetworkEngine engine) {
            if (title == null || type == null || fields == null || links == null) {
                /*
                 * The state of this entity has changed and is invalid.
                 */
                return null;
            }
            Model model = new Model(title, type, fields, convert(links, k -> k, URI::create));
            return NetworkEngine.createEntity(engine, entityType, model);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Entity entity = (Entity) o;
            return Objects.equals(title, entity.title) && Objects.equals(type, entity.type) && Objects.equals(fields, entity.fields) && Objects.equals(links, entity.links);
        }

        @Override
        public int hashCode() {
            return Objects.hash(title, type, fields, links);
        }

        @Override
        public String toString() {
            return "Entity{" +
                    "title='" + title + '\'' +
                    ", type='" + type + '\'' +
                    ", fields=" + fields +
                    ", links=" + links +
                    '}';
        }

        @Override
        public int compareTo(@NotNull Entity entity) {
            return (title != null ? title : "").compareTo((entity.title != null ? entity.title : ""));
        }
    }
}
