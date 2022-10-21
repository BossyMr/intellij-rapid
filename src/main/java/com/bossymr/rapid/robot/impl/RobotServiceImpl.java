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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@State(name = "robot", storages = {
        @Storage("robot/robot.xml")
})
public class RobotServiceImpl implements RobotService {

    private static final Logger LOG = Logger.getInstance(RobotService.class);
    private final Project project;
    private Robot robot;
    private RobotService.State state = new RobotService.State();

    private final Map<String, RapidSymbol> symbols;
    private final Type type;

    public RobotServiceImpl(@NotNull Project project) {
        this.project = project;
        this.symbols = new HashMap<>();
        this.type = buildSymbols();
    }

    public @NotNull Map<String, RapidSymbol> getSymbols() {
        return symbols;
    }

    private @NotNull Type buildSymbols() {
        symbols.put("num", new LightAtomic(project, "num"));
        RapidType numberType = new RapidType((RapidStructure) symbols.get("num"));
        symbols.put("dnum", new LightAtomic(project, "dnum"));
        RapidType doubleType = new RapidType((RapidStructure) symbols.get("dnum"));
        symbols.put("bool", new LightAtomic(project, "bool"));
        RapidType boolType = new RapidType((RapidStructure) symbols.get("bool"));
        symbols.put("string", new LightAtomic(project, "string"));
        RapidType stringType = new RapidType((RapidStructure) symbols.get("string"));
        symbols.put("pos", new LightRecord(project, "pos", List.of(new LightComponent(project, "x", numberType), new LightComponent(project, "y", numberType), new LightComponent(project, "z", numberType))));
        RapidType posType = new RapidType((RapidStructure) symbols.get("pos"));
        symbols.put("orient", new LightRecord(project, "orient", List.of(new LightComponent(project, "q1", numberType), new LightComponent(project, "q2", numberType), new LightComponent(project, "q3", numberType), new LightComponent(project, "q4", numberType))));
        RapidType orientType = new RapidType((RapidStructure) symbols.get("pos"));
        symbols.put("pose", new LightRecord(project, "pose", List.of(new LightComponent(project, "trans", posType), new LightComponent(project, "rot", orientType))));
        RapidType poseType = new RapidType((RapidStructure) symbols.get("pose"));
        return new Type() {
            @Override
            public @NotNull RapidType getNumber() {
                return numberType;
            }

            @Override
            public @NotNull RapidType getDouble() {
                return doubleType;
            }

            @Override
            public @NotNull RapidType getString() {
                return stringType;
            }

            @Override
            public @NotNull RapidType getBool() {
                return boolType;
            }

            @Override
            public @NotNull RapidType getPosition() {
                return posType;
            }

            @Override
            public @NotNull RapidType getOrientation() {
                return orientType;
            }

            @Override
            public @NotNull RapidType getTransformation() {
                return poseType;
            }
        };
    }

    @Override
    public @NotNull Type getType() {
        return type;
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
    public void disconnect() throws IOException {
        if (state.robotState != null) {
            LOG.info("Disconnecting from persisted robot: " + state.robotState.path);
            Robot robot = getRobot().orElseThrow();
            if (robot.isConnected()) robot.disconnect();
            this.state.robotState = null;
            this.robot = null;
            getTopic().onDisconnect();
        } else {
            LOG.warn("Attempt to disconnect to non-existent robot");
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
        return project.getMessageBus().syncPublisher(RobotTopic.ROBOT_TOPIC);
    }

    @Override
    public @Nullable RobotService.State getState() {
        return state;
    }

    @Override
    public void loadState(@NotNull State state) {
        this.state = state;
    }
}
