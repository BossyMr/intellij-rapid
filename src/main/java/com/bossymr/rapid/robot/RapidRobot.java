package com.bossymr.rapid.robot;

import com.bossymr.network.ResponseStatusException;
import com.bossymr.network.client.EntityModel;
import com.bossymr.network.client.NetworkManager;
import com.bossymr.network.client.proxy.EntityProxy;
import com.bossymr.network.client.proxy.ProxyException;
import com.bossymr.network.client.security.Credentials;
import com.bossymr.rapid.language.RapidFileType;
import com.bossymr.rapid.language.symbol.RapidTask;
import com.bossymr.rapid.language.symbol.virtual.VirtualSymbol;
import com.bossymr.rapid.robot.impl.RapidTaskImpl;
import com.bossymr.rapid.robot.impl.SymbolConverter;
import com.bossymr.rapid.robot.network.ControllerService;
import com.bossymr.rapid.robot.network.Identity;
import com.bossymr.rapid.robot.network.LoadProgramMode;
import com.bossymr.rapid.robot.network.robotware.mastership.MastershipType;
import com.bossymr.rapid.robot.network.robotware.rapid.RapidService;
import com.bossymr.rapid.robot.network.robotware.rapid.symbol.SymbolModel;
import com.bossymr.rapid.robot.network.robotware.rapid.symbol.SymbolQuery;
import com.bossymr.rapid.robot.network.robotware.rapid.symbol.SymbolSearchMethod;
import com.bossymr.rapid.robot.network.robotware.rapid.symbol.SymbolType;
import com.bossymr.rapid.robot.network.robotware.rapid.task.Task;
import com.bossymr.rapid.robot.network.robotware.rapid.task.TaskService;
import com.bossymr.rapid.robot.network.robotware.rapid.task.module.ModuleEntity;
import com.bossymr.rapid.robot.network.robotware.rapid.task.module.ModuleInfo;
import com.bossymr.rapid.robot.network.robotware.rapid.task.program.Program;
import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.credentialStore.CredentialAttributesKt;
import com.intellij.credentialStore.OneTimeString;
import com.intellij.ide.passwordSafe.PasswordSafe;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.application.WriteAction;
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
import java.util.function.Function;
import java.util.stream.Collectors;

public class RapidRobot implements Disposable {

    @Topic.AppLevel
    public static final Topic<StateListener> STATE_TOPIC = Topic.create("Robot State", StateListener.class);
    private @NotNull State state = new State();
    private @Nullable NetworkManager manager;
    private @NotNull Set<RapidTask> tasks;
    private @NotNull Map<String, VirtualSymbol> symbols;

    private RapidRobot(@Nullable NetworkManager manager, @NotNull State state) {
        if (state.name == null || state.path == null || state.symbols == null || state.cache == null) {
            String symbols = "(symbols: " + (state.symbols != null ? state.symbols.size() : "null") + ")";
            String cache = "(cache: " + (state.cache != null ? state.cache.size() : "null") + ")";
            throw new IllegalArgumentException("State '" + state + "'" + symbols + " " + cache + " is invalid");
        }
        this.manager = manager;
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
    public static @NotNull RapidRobot connect(@NotNull URI path, @NotNull Credentials credentials) throws IOException, InterruptedException {
        setCredentials(path, credentials);
        NetworkManager manager = NetworkManager.newBuilder(path)
                .setCredentials(credentials)
                .build();
        State state = getState(path, manager);
        RapidRobot robot = new RapidRobot(manager, state);
        RobotEventListener.publish().onRefresh(robot, manager);
        robot.download();
        return robot;
    }

    private static <T> @NotNull CompletableFuture<Void> combine(@NotNull Collection<? extends T> elements, @NotNull Function<T, CompletableFuture<Void>> converter) {
        return CompletableFuture.allOf(elements.stream().map(converter).filter(Objects::nonNull).toList().toArray(CompletableFuture[]::new));
    }

    private static @NotNull State getState(@NotNull URI path, @NotNull NetworkManager manager) throws IOException, InterruptedException {
        State state = new State();
        state.path = path.getScheme() + "://" + path.getHost() + ":" + path.getPort();
        state.cache = new HashSet<>();
        Identity identity = manager.createService(ControllerService.class).getIdentity().get();
        state.name = identity.getName();
        SymbolQuery query = new SymbolQuery()
                .setRecursive(true)
                .setMethod(SymbolSearchMethod.BLOCK)
                .setBlock("RAPID")
                .setSymbolType(SymbolType.ANY);
        List<SymbolModel> symbols = manager.createService(RapidService.class).findSymbols(query).get();
        state.symbols = symbols.stream().map(Entity::convert).collect(Collectors.toSet());
        Map<String, Set<String>> states = new HashMap<>();
        for (SymbolModel symbol : symbols) {
            String title = symbol.getTitle();
            int index = title.lastIndexOf('/');
            String address = title.substring(0, index);
            states.computeIfAbsent(address, value -> new HashSet<>());
            states.get(address).add(title.substring(index + 1));
        }
        // Fetch symbols which were not included in original request.
        // For example, if a component exists for a routine which does not exist, fetch it.
        for (var entry : states.entrySet()) {
            if (entry.getKey().equals("RAPID")) continue;
            String name = entry.getKey().substring(entry.getKey().lastIndexOf('/') + 1);
            if (states.get("RAPID").contains(name)) continue;
            SymbolModel model = getSymbol(manager, name);
            Objects.requireNonNull(model);
            Entity entity = Entity.convert(model);
            state.symbols.add(entity);
        }
        return state;
    }

    private static @Nullable SymbolModel getSymbol(@NotNull NetworkManager manager, @NotNull String name) throws IOException, InterruptedException {
        try {
            return manager.createService(RapidService.class).findSymbol(name).get();
        } catch (ResponseStatusException e) {
            if (e.getResponse().statusCode() == 400) {
                return null;
            }
            throw e;
        }
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
        return manager != null;
    }

    /**
     * Returns the network engine used to communicate with the remote robot.
     *
     * @return the network engine, or {@code null} if this robot is not currently connected.
     */
    public @Nullable NetworkManager getNetworkManager() {
        return manager;
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
    public @Nullable VirtualSymbol getSymbol(@NotNull String name) throws IOException, InterruptedException {
        if (symbols.containsKey(name.toLowerCase())) {
            return symbols.get(name);
        }
        if (state.cache != null && state.cache.contains(name.toLowerCase())) {
            return null;
        }
        if (manager == null) {
            return null;
        }
        SymbolModel model = getSymbol(manager, "RAPID" + "/" + name);
        if (model == null) {
            if (state.cache == null) {
                state.cache = new HashSet<>();
            }
            state.cache.add(name.toLowerCase());
            return null;
        }
        VirtualSymbol symbol = SymbolConverter.getSymbol(model);
        symbols.put(symbol.getName().toLowerCase(), symbol);
        if (state.symbols == null) {
            state.symbols = new HashSet<>();
        }
        state.symbols.add(Entity.convert(model));
        RobotEventListener.publish().onSymbol(this, symbol);
        return symbol;
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

    public void download() throws IOException, InterruptedException {
        if (manager == null) {
            throw new IllegalStateException("Robot is not connected");
        }
        VirtualFile finalDirectory;
        Set<RapidTask> updated;
        try (CloseableDirectory directory = new CloseableDirectory("download")) {
            File file = Path.of(PathManager.getSystemPath(), "robot").toFile();
            if (file.exists()) {
                FileUtil.delete(file);
            }
            finalDirectory = LocalFileSystem.getInstance().findFileByNioFile(PathManager.getSystemDir());
            if (finalDirectory == null) {
                throw new IOException();
            }
            updated = new HashSet<>();
            List<Task> tasks = manager.createService(TaskService.class).getTasks().get();
            for (Task task : tasks) {
                File temporaryTask = directory.getVirtualFile().toNioPath().resolve(task.getName()).toFile();
                File finalTask = finalDirectory.toNioPath().resolve("robot").resolve(task.getName()).toFile();
                if (!(WriteAction.computeAndWait(() -> FileUtil.createDirectory(temporaryTask)))) {
                    throw new IllegalStateException();
                }
                RapidTask local = new RapidTaskImpl(task.getName(), finalTask, new HashSet<>());
                List<ModuleInfo> modules = task.getModules().get();
                for (ModuleInfo moduleInfo : modules) {
                    ModuleEntity module = moduleInfo.getModule();
                    try (CloseableMastership ignored = CloseableMastership.withMastership(manager, MastershipType.RAPID)) {
                        module.save(module.getName(), temporaryTask.toPath().toString()).get();
                    }
                    File result = finalTask.toPath().resolve(module.getName() + RapidFileType.DEFAULT_DOT_EXTENSION).toFile();
                    local.getFiles().add(result);
                }
                LocalFileSystem.getInstance().refreshIoFiles(local.getFiles());
            }
            WriteAction.runAndWait(() -> {
                VirtualFile directoryChild = finalDirectory.findChild("robot");
                if (directoryChild != null) {
                    directoryChild.delete(this);
                }
                directory.getVirtualFile().copy(this, finalDirectory, "robot");
            });
        }
        finalDirectory.refresh(false, true);
        RobotEventListener.publish().onDownload(this);
        this.tasks = updated;
    }

    public void upload() throws IOException, InterruptedException {
        LocalFileSystem localFileSystem = LocalFileSystem.getInstance();
        for (RapidTask task : getTasks()) {
            Set<VirtualFile> virtualFiles = task.getFiles().stream()
                    .map(localFileSystem::findFileByIoFile)
                    .collect(Collectors.toSet());
            upload(task, virtualFiles);
        }
    }

    public void upload(@NotNull RapidTask task, @NotNull Set<VirtualFile> modules) throws IOException, InterruptedException {
        if (manager == null) {
            throw new IllegalStateException("Robot is not connected");
        }
        try (CloseableDirectory temporaryDirectory = new CloseableDirectory("upload")) {
            LocalFileSystem.getInstance().refreshFiles(modules);
            Task remote = manager.createService(TaskService.class).getTask(task.getName()).get();
            Program program = remote.getProgram().get();
            File file = temporaryDirectory.getVirtualFile().toNioPath().resolve(program.getName() + ".pgf").toFile();
            WriteAction.runAndWait(() -> {
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                    writer.write("<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?>\r\n");
                    writer.write("<Program>\r\n");
                    for (VirtualFile module : modules) {
                        writer.write("\t<Module>" + module.getName() + "</Module>\r\n");
                        module.copy(this, temporaryDirectory.getVirtualFile(), module.getName());
                    }
                    writer.write("</Program>");
                }
            });
            try (CloseableMastership ignored = CloseableMastership.withMastership(manager, MastershipType.RAPID)) {
                program.load(file.getPath(), LoadProgramMode.REPLACE).get();
            }
            RobotEventListener.publish().onUpload(this);
        }
    }

    public @NotNull NetworkManager reconnect() throws IllegalStateException, IOException, InterruptedException {
        URI path = getPath();
        Credentials credentials = getCredentials(path);
        if (credentials == null) {
            throw new IllegalStateException("Credentials for '" + path + "' are not persisted");
        }
        return reconnect(path, credentials);
    }

    public @NotNull NetworkManager reconnect(@NotNull Credentials credentials) throws IOException, InterruptedException {
        URI path = getPath();
        setCredentials(path, credentials);
        return reconnect(path, credentials);
    }

    private @NotNull NetworkManager reconnect(@NotNull URI path, @NotNull Credentials credentials) throws IOException, InterruptedException {
        NetworkManager manager = NetworkManager.newBuilder(path)
                .setCredentials(credentials)
                .build();
        State state = getState(path, manager);
        Objects.requireNonNull(state.symbols);
        List<SymbolModel> models = state.symbols.stream()
                .map(symbol -> symbol.convert(SymbolModel.class, manager))
                .toList();
        this.symbols = SymbolConverter.getSymbols(models);
        setState(state);
        this.tasks = getPersistedTasks();
        RobotEventListener.publish().onRefresh(this, manager);
        return this.manager = manager;
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

    public void disconnect() throws IOException, InterruptedException {
        if (manager == null) {
            return;
        }
        manager.close();
        manager = null;
        RobotEventListener.publish().onDisconnect(this);
    }

    @Override
    public void dispose() {}

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
         * a cache, and should not be queried again.
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
         * @param entity the entity.
         * @param <T> the entity type.
         * @return the state.
         */
        public static <T> @NotNull Entity convert(@NotNull T entity) {
            if (!(entity instanceof EntityProxy proxy)) {
                throw new IllegalArgumentException();
            }
            EntityModel model = proxy.getModel();
            return convert(model);
        }

        private static @NotNull Entity convert(EntityModel model) {
            return new Entity(model.title(), model.type(), model.properties(), convert(model.references(), k -> k, URI::toString));
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
        public <T> @Nullable T convert(@NotNull Class<T> entityType, @Nullable NetworkManager engine) {
            if (title == null || type == null || fields == null || links == null) {
                /*
                 * The state of this entity has changed and is invalid.
                 */
                return null;
            }
            EntityModel model = new EntityModel(title, type, convert(links, k -> k, URI::create), fields);
            if (engine == null) {
                return NetworkManager.createLightEntity(entityType, model);
            } else {
                return engine.createEntity(entityType, model);
            }
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
