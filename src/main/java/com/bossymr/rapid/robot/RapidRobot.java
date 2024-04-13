package com.bossymr.rapid.robot;

import com.bossymr.rapid.language.RapidFileType;
import com.bossymr.rapid.language.symbol.RapidSymbol;
import com.bossymr.rapid.language.symbol.RapidTask;
import com.bossymr.rapid.language.symbol.physical.PhysicalModule;
import com.bossymr.rapid.language.symbol.resolve.ResolveService;
import com.bossymr.rapid.language.symbol.virtual.VirtualSymbol;
import com.bossymr.rapid.robot.api.NetworkManager;
import com.bossymr.rapid.robot.api.ResponseStatusException;
import com.bossymr.rapid.robot.api.client.EntityModel;
import com.bossymr.rapid.robot.api.client.HeavyNetworkManager;
import com.bossymr.rapid.robot.api.client.proxy.EntityProxy;
import com.bossymr.rapid.robot.api.client.security.Credentials;
import com.bossymr.rapid.robot.impl.VirtualSymbolFactory;
import com.bossymr.rapid.robot.network.ControllerService;
import com.bossymr.rapid.robot.network.Identity;
import com.bossymr.rapid.robot.network.LoadProgramMode;
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
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.search.LocalSearchScope;
import com.intellij.psi.search.SearchScope;
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
import java.util.stream.Collectors;

public class RapidRobot implements Disposable {

    @Topic.AppLevel
    public static final Topic<StateListener> STATE_TOPIC = Topic.create("Robot State", StateListener.class);

    private @NotNull State state = new State();
    private @Nullable NetworkManager manager;
    private @NotNull Set<RapidTask> tasks;
    private @NotNull Map<String, VirtualSymbol> symbols;

    private RapidRobot(@NotNull State state) {
        if (state.name == null || state.path == null || state.symbols == null || state.cache == null) {
            throw new IllegalArgumentException("State '" + state + " is invalid");
        }
        setState(state);
        this.symbols = VirtualSymbolFactory.getSymbols(state.symbols.stream()
                                                                    .map(symbol -> symbol.convert(SymbolModel.class, null))
                                                                    .filter(Objects::nonNull)
                                                                    .toList());
        this.tasks = getPersistedTasks();
    }

    /**
     * Create a new robot with the specified state.
     *
     * @return the robot.
     * @throws IllegalArgumentException if the specified state is invalid.
     */
    public static @NotNull RapidRobot create(@NotNull State state) throws IllegalArgumentException {
        return new RapidRobot(state);
    }

    /**
     * Create a new robot by connecting to the specified robot.
     *
     * @param path the path to the robot.
     * @param credentials the credentials to authenticate with.
     * @return the robot.
     * @throws IOException if an I/O error occurs.
     * @throws InterruptedException if the current thread is interrupted.
     */
    public static @NotNull RapidRobot connect(@NotNull URI path, @NotNull Credentials credentials) throws IOException, InterruptedException {
        setCredentials(path, credentials);
        NetworkManager manager = new HeavyNetworkManager(path, credentials);
        RobotNetworkAction action = new RobotNetworkAction(manager);
        State state = getState(path, action);
        RapidRobot robot = new RapidRobot(state);
        RobotEventListener.publish().onRefresh(robot, manager);
        robot.setManager(action);
        robot.download();
        return robot;
    }

    private static @NotNull State getState(@NotNull URI path, @NotNull NetworkManager manager) throws IOException, InterruptedException {
        State state = new State();
        state.path = path.getScheme() + "://" + path.getHost() + ":" + path.getPort();
        state.cache = new HashSet<>();
        Identity identity;
        try {
            identity = manager.createService(ControllerService.class).getIdentity().get();
        } catch (IllegalArgumentException e) {
            throw new IOException(e);
        }
        state.name = identity.getName();
        SymbolQuery query = new SymbolQuery()
                .setRecursive(true)
                .setMethod(SymbolSearchMethod.BLOCK)
                .setBlock("RAPID")
                .setSymbolType(SymbolType.ANY);
        List<SymbolModel> symbols = manager.createService(RapidService.class).findSymbols(query).get();
        state.symbols = symbols.stream()
                               .map(Entity::convert)
                               .collect(Collectors.toSet());
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
            SymbolModel model = getSymbol(manager, "RAPID" + "/" + name);
            Objects.requireNonNull(model, entry.toString());
            Entity entity = Entity.convert(model);
            state.symbols.add(entity);
        }
        return state;
    }

    private static @Nullable SymbolModel getSymbol(@NotNull NetworkManager manager, @NotNull String name) throws IOException, InterruptedException {
        try {
            return manager.createService(RapidService.class).findSymbol(name).get();
        } catch (ResponseStatusException e) {
            if (e.getResponse().code() == 400) {
                return null;
            }
            throw e;
        }
    }

    private static @NotNull CredentialAttributes createCredentialsAttributes(@NotNull URI path, @Nullable String username) {
        return new CredentialAttributes(CredentialAttributesKt.generateServiceName("intellij-rapid", path.toString()), username);
    }

    public static @Nullable Credentials getCredentials(@NotNull URI path, @Nullable String username) {
        CredentialAttributes credentialsAttributes = createCredentialsAttributes(path, username);
        var credentials = PasswordSafe.getInstance().get(credentialsAttributes);
        if (credentials == null) {
            return null;
        }
        username = credentials.getUserName();
        OneTimeString password = credentials.getPassword();
        if (username == null || password == null) {
            return null;
        }
        return new Credentials(username, password.toCharArray());
    }

    public static void setCredentials(@NotNull URI path, @NotNull Credentials credentials) {
        CredentialAttributes credentialsAttributes = createCredentialsAttributes(path, credentials.username());
        PasswordSafe.getInstance().setPassword(credentialsAttributes, new String(credentials.password()));
    }

    public @Nullable String getUsername() {
        Credentials credentials = getCredentials(getPath(), null);
        return credentials != null ? credentials.username() : null;
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
        return tasks.stream().filter(RapidTask::isValid).collect(Collectors.toSet());
    }

    public @NotNull SearchScope getSearchScope(@NotNull Project project) {
        PhysicalModule[] modules = getTasks().stream()
                                             .flatMap(task -> task.getModules(project).stream())
                                             .toList()
                                             .toArray(PhysicalModule[]::new);
        return new LocalSearchScope(modules);
    }

    /**
     * Returns the symbol in this robot with the specified name. If a symbol is found, it is persisted and returned by
     * subsequent calls to {@link #getSymbols()}.
     *
     * @param name the name of the symbol.
     * @return the symbol, or {@code null} if a symbol was not found with the specified name.
     */
    public @Nullable VirtualSymbol getSymbol(@NotNull String name) throws IOException, InterruptedException {
        int index = name.indexOf("/");
        String parentName = name;
        String childName = null;
        if (index >= 0) {
            if (index != name.lastIndexOf("/")) {
                throw new IllegalArgumentException("Could not retrieve symbol: " + name);
            }
            parentName = name.substring(0, index);
            childName = name.substring(index + 1);
        }
        if (symbols.containsKey(parentName.toLowerCase())) {
            // The symbol already exists.
            VirtualSymbol symbol = symbols.get(parentName.toLowerCase());
            if (childName != null) {
                List<RapidSymbol> results = ResolveService.getChildSymbol(symbol, childName);
                return results.isEmpty() ? null : (VirtualSymbol) results.get(0);
            }
            return symbol;
        }
        if (state.cache != null && state.cache.contains(name.toLowerCase())) {
            // The symbol does not exist. Due to all symbols not being provided by the robot
            // all unresolved symbols need to be requested individually. If they do not exist,
            // they are added to a cache, so they aren't requested again.
            return null;
        }
        if (manager == null) {
            // The robot is not connected.
            return null;
        }
        SymbolModel model = getSymbol(manager, "RAPID" + "/" + parentName);
        if (model == null) {
            // The symbol does not exist.
            if (state.cache == null) {
                state.cache = new HashSet<>();
            }
            state.cache.add(name.toLowerCase());
            return null;
        }
        VirtualSymbol symbol = VirtualSymbolFactory.getSymbol(model);
        symbols.put(parentName.toLowerCase(), symbol);
        if (state.symbols == null) {
            state.symbols = new HashSet<>();
        }
        state.symbols.add(Entity.convert(model));
        RobotEventListener.publish().onSymbol(this, symbol);
        if (childName != null) {
            List<RapidSymbol> results = ResolveService.getChildSymbol(symbol, childName);
            return results.isEmpty() ? null : (VirtualSymbol) results.get(0);
        }
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
        Set<RapidTask> updated = new HashSet<>();
        File directory = FileUtil.createTempDirectory("intellij-rapid", "download", true);
        File file = getDefaultPath().toFile();
        if (file.exists()) {
            FileUtil.delete(file);
        }
        List<Task> tasks = manager.createService(TaskService.class).getTasks().get();
        for (Task task : tasks) {
            File temporaryTask = directory.toPath().resolve(task.getName()).toFile();
            File finalTask = file.toPath().resolve(task.getName()).toFile();
            if (!(WriteAction.computeAndWait(() -> FileUtil.createDirectory(temporaryTask)))) {
                throw new IllegalStateException();
            }
            RapidTask local = new RapidTask(task.getName(), finalTask, new HashSet<>());
            List<ModuleInfo> modules = task.getModules().get();
            for (ModuleInfo moduleInfo : modules) {
                ModuleEntity module = moduleInfo.getModule().get();
                module.save(module.getName(), temporaryTask.toPath().toString()).get();
                File result = finalTask.toPath().resolve(module.getName() + RapidFileType.DEFAULT_DOT_EXTENSION).toFile();
                local.getFiles().add(result);
            }
            updated.add(local);
        }
        WriteAction.runAndWait(() -> {
            if (file.exists()) {
                FileUtil.delete(file);
            }
            FileUtil.copyDirContent(directory, file);
            for (RapidTask task : updated) {
                for (File taskFile : task.getFiles()) {
                    VirtualFile virtualFile = VirtualFileManager.getInstance().refreshAndFindFileByNioPath(taskFile.toPath());
                    if (virtualFile == null) {
                        throw new IllegalStateException("File '" + taskFile + "' not found");
                    }
                }
            }
        });
        RobotEventListener.publish().onDownload(this);
        this.tasks = updated;
    }

    public void upload() throws IOException, InterruptedException {
        for (RapidTask task : getTasks()) {
            upload(task);
        }
    }

    public void upload(@NotNull RapidTask task) throws IOException, InterruptedException {
        upload(task, task.getFiles());
    }

    public void upload(@NotNull RapidTask task, @NotNull Set<File> modules) throws IOException, InterruptedException {
        if (manager == null) {
            throw new IllegalStateException("Robot is not connected");
        }
        File directory = FileUtil.createTempDirectory("intellij-rapid", "upload", true);
        Task remote = manager.createService(TaskService.class).getTask(task.getName()).get();
        Program program = remote.getProgram().get();
        File file = directory.toPath().resolve(program.getName() + ".pgf").toFile();
        WriteAction.runAndWait(() -> {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write("<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?>\r\n");
                writer.write("<Program>\r\n");
                for (File module : modules) {
                    writer.write("\t<Module>" + module.getName() + "</Module>\r\n");
                    FileUtil.copy(module, directory.toPath().resolve(module.getName()).toFile());
                }
                writer.write("</Program>");
            }
        });
        program.load(file.getPath(), LoadProgramMode.REPLACE).get();
        RobotEventListener.publish().onUpload(this);
    }

    /**
     * Reconnect to this robot with persisted credentials.
     *
     * @return the connection to the robot.
     * @throws IllegalStateException if no credentials are persisted for this robot.
     * @throws IOException if an I/O error occurs.
     * @throws InterruptedException if the current thread is interrupted.
     */
    public @NotNull NetworkManager reconnect() throws IllegalStateException, IOException, InterruptedException {
        URI path = getPath();
        Credentials credentials = getCredentials(path, null);
        if (credentials == null) {
            throw new IllegalStateException("Credentials for '" + path + "' are not persisted");
        }
        return reconnect(path, credentials);
    }

    /**
     * Reconnect to this robot with the specified credentials.
     *
     * @param credentials the new credentials.
     * @return the connection to the robot.
     * @throws IOException if an I/O error occurs.
     * @throws InterruptedException if the current thread is interrupted.
     */
    public @NotNull NetworkManager reconnect(@NotNull Credentials credentials) throws IOException, InterruptedException {
        URI path = getPath();
        setCredentials(path, credentials);
        return reconnect(path, credentials);
    }

    private @NotNull NetworkManager reconnect(@NotNull URI path, @NotNull Credentials credentials) throws IOException, InterruptedException {
        NetworkManager manager = new HeavyNetworkManager(path, credentials);
        RobotNetworkAction networkAction = new RobotNetworkAction(manager);
        State state = getState(path, networkAction);
        Objects.requireNonNull(state.symbols);
        List<SymbolModel> models = state.symbols.stream()
                                                .map(symbol -> symbol.convert(SymbolModel.class, manager))
                                                .toList();
        this.symbols = VirtualSymbolFactory.getSymbols(models);
        setState(state);
        this.tasks = getPersistedTasks();
        RobotEventListener.publish().onRefresh(this, networkAction);
        setManager(networkAction);
        if (!(getLocalModules(tasks).equals(getRemoteModules(networkAction)))) {
            download();
        }
        return networkAction;
    }

    private @NotNull List<String> getLocalModules(@NotNull Set<RapidTask> tasks) {
        List<String> modules = new ArrayList<>();
        for (RapidTask task : tasks) {
            for (File module : task.getFiles()) {
                String name = module.getName();
                modules.add(task.getName() + ":" + name.substring(0, name.lastIndexOf('.')));
            }
        }
        return modules;
    }

    private @NotNull List<String> getRemoteModules(@NotNull NetworkManager manager) throws IOException, InterruptedException {
        TaskService service = manager.createService(TaskService.class);
        List<Task> tasks = service.getTasks().get();
        List<String> modules = new ArrayList<>();
        for (Task task : tasks) {
            List<ModuleInfo> infos = task.getModules().get();
            for (ModuleInfo module : infos) {
                modules.add(task.getName() + ":" + module.getName());
            }
        }
        return modules;
    }

    private void setManager(@NotNull NetworkManager manager) {
        this.manager = manager;
    }

    private @NotNull Path getDefaultPath() {
        return PathManager.getPluginsDir().resolve("Rapid").resolve("robot");
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
                        RapidTask task = new RapidTask(child.getName(), child, files);
                        tasks.add(task);
                    }
                }
            }
        }
        return tasks;
    }

    /**
     * Returns the path to this robot.
     *
     * @return the path to this robot.
     */
    public @NotNull URI getPath() {
        if (state.path == null) {
            throw new IllegalStateException("State '" + state + "' is invalid");
        }
        try {
            return new URI(state.path);
        } catch (URISyntaxException e) {
            throw new IllegalStateException("State '" + state.path + "' is invalid", e);
        }
    }

    /**
     * Disconnects from this robot. If this robot is not connected, this method will return.
     *
     * @throws IOException if an I/O error occurs.
     * @throws InterruptedException if the current thread is interrupted.
     */
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
     * A model which represents the persisted state of a {@code RapidRobot}.
     * <p>
     * Each field might be represented by {@code null} if the field is not given a value when initialized.
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
         * Due to an issue on the robot. All symbols are not returned when asking for a list of all symbols. However,
         * when asking the robot for a symbol with a specific name, it appears to always return the correct result. As
         * a result, if a symbol hasn't been resolved to any existing symbol, a request is sent to the robot, asking for
         * a specific symbol with that name. If a symbol with that name wasn't found, it is added to this cache, so
         * that the request won't be retried.
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

        /**
         * This constructor is used for reflection to be able to create a new empty object and should not be used.
         */
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
            Map<String, String> references = new HashMap<>();
            model.references().forEach((name, value) -> references.put(name, value.toString()));
            return new Entity(model.title(), model.type(), model.properties(), Map.copyOf(references));
        }

        /**
         * Converts this state into an entity model of the specified type. The persisted state might be invalid, if the
         * state was edited manually.
         *
         * @param entityType the entity type.
         * @param manager the network manager, used to send requests from the created entity.
         * @param <T> the entity type.
         * @return the entity, or {@code null} if this entity is invalid.
         */
        public <T> @Nullable T convert(@NotNull Class<T> entityType, @Nullable NetworkManager manager) {
            if (title == null || type == null || fields == null || links == null) {
                /*
                 * The state of this entity has changed and is invalid.
                 */
                return null;
            }
            Map<String, URI> references = new HashMap<>();
            links.forEach((name, value) -> references.put(name, URI.create(value)));
            EntityModel model = new EntityModel(title, type, Map.copyOf(references), Map.copyOf(fields));
            if (manager == null) {
                return NetworkManager.createLightEntity(entityType, model);
            } else {
                return manager.createEntity(entityType, model);
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
