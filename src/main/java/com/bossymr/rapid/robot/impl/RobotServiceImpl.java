package com.bossymr.rapid.robot.impl;

import com.bossymr.rapid.language.psi.RapidStructure;
import com.bossymr.rapid.language.psi.RapidSymbol;
import com.bossymr.rapid.language.psi.RapidType;
import com.bossymr.rapid.language.psi.light.LightAtomic;
import com.bossymr.rapid.language.psi.light.LightComponent;
import com.bossymr.rapid.language.psi.light.LightRecord;
import com.bossymr.rapid.robot.Robot;
import com.bossymr.rapid.robot.RobotService;
import com.bossymr.rapid.robot.RobotTopic;
import com.bossymr.rapid.robot.network.controller.Controller;
import com.intellij.credentialStore.Credentials;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URI;
import java.util.*;

@State(name = "robot", storages = {
        @Storage("robot/robot.xml")
})
public class RobotServiceImpl implements RobotService {

    private static final Logger LOG = Logger.getInstance(RobotService.class);
    private final Project project;
    private final Map<String, RapidSymbol> symbols;
    private Robot robot;
    private RobotService.State state = new RobotService.State();

    public RobotServiceImpl(@NotNull Project project) {
        this.project = project;
        this.symbols = new HashMap<>();
        buildSymbols();
    }

    public @NotNull Map<String, RapidSymbol> getMap() {
        return symbols;
    }

    @Override
    public @NotNull Set<RapidSymbol> getSymbols() {
        Optional<Robot> optional = getRobot();
        if (optional.isPresent()) {
            RobotImpl implementation = (RobotImpl) optional.get();
            return implementation.getSymbols();
        }
        return Set.copyOf(symbols.values());
    }

    @Override
    public @NotNull Optional<RapidSymbol> getSymbol(@NotNull String name) {
        Optional<Robot> optional = getRobot();
        if (optional.isPresent()) {
            RobotImpl implementation = (RobotImpl) optional.get();
            return implementation.getSymbol(name);
        }
        return symbols.containsKey(name) ? Optional.of(symbols.get(name)) : Optional.empty();
    }

    private void buildSymbols() {
        symbols.put("num", new LightAtomic(project, "num"));
        symbols.put("dnum", new LightAtomic(project, "dnum"));
        symbols.put("bool", new LightAtomic(project, "bool"));
        symbols.put("string", new LightAtomic(project, "string"));
        symbols.put("pos", new LightRecord(project, "pos", List.of(new LightComponent(project, "x", getType(DataType.NUMBER)), new LightComponent(project, "y", getType(DataType.NUMBER)), new LightComponent(project, "z", getType(DataType.NUMBER)))));
        symbols.put("orient", new LightRecord(project, "orient", List.of(new LightComponent(project, "q1", getType(DataType.NUMBER)), new LightComponent(project, "q2", getType(DataType.NUMBER)), new LightComponent(project, "q3", getType(DataType.NUMBER)), new LightComponent(project, "q4", getType(DataType.NUMBER)))));
        symbols.put("pose", new LightRecord(project, "pose", List.of(new LightComponent(project, "trans", getType(DataType.POSITION)), new LightComponent(project, "rot", getType(DataType.ORIENTATION)))));
    }

    @Override
    public @NotNull RapidType getType(@NotNull DataType dataType) {
        return switch (dataType) {
            case NUMBER -> getType("num");
            case DOUBLE -> getType("dnum");
            case STRING -> getType("string");
            case BOOLEAN -> getType("bool");
            case POSITION -> getType("pos");
            case ORIENTATION -> getType("orient");
            case TRANSFORMATION -> getType("pose");
        };
    }

    private @NotNull RapidType getType(@NotNull String name) {
        RapidSymbol symbol = symbols.get(name);
        if (symbol instanceof RapidStructure structure) {
            return new RapidType(structure);
        }
        throw new IllegalStateException(name);
    }

    @Override
    public @NotNull Optional<Robot> getRobot() {
        if (robot != null) {
            LOG.debug("Retrieving connected robot: " + state.robotState.path);
            return Optional.of(robot);
        }
        if (state.robotState != null) {
            LOG.debug("Connecting to persisted robot: " + state.robotState.path);
            return Optional.of(robot = new RobotImpl(project, state.robotState));
        }
        return Optional.empty();
    }

    @Override
    public void delete() throws IOException {
        if (state.robotState != null) {
            LOG.info("Disconnecting from persisted robot: " + state.robotState.path);
            Robot robot = getRobot().orElseThrow();
            if (robot.isConnected()) robot.disconnect();
            this.state.robotState = null;
            this.robot = null;
            getTopic().onDisconnect();
        } else {
            LOG.warn("Attempting to delete non-existent robot");
        }
    }

    @Override
    public @NotNull Robot connect(@NotNull URI path, @NotNull Credentials credentials) throws IOException {
        LOG.debug("Connecting to new robot:" + path);
        Controller controller = RobotUtil.getController(path, credentials);
        state.robotState = RobotUtil.getState(controller);
        robot = new RobotImpl(project, state.robotState, controller);
        getTopic().onConnect(robot);
        return robot;
    }

    private @NotNull RobotTopic getTopic() {
        return RobotTopic.publish(project);
    }

    @Override
    public @Nullable RobotService.State getState() {
        return state;
    }

    @Override
    public void loadState(@NotNull State state) {
        this.state = state;
    }

    @Override
    public void dispose() {
        Optional<Robot> optionalRobot = getRobot();
        if (optionalRobot.isPresent()) {
            URI path = optionalRobot.get().getPath();
            try {
                optionalRobot.get().disconnect();
            } catch (IOException e) {
                LOG.error("Failed to disconnect robot: " + path);
            }
        }
    }
}
