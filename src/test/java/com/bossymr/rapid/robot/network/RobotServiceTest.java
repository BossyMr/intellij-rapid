package com.bossymr.rapid.robot.network;

import com.bossymr.network.ResponseStatusException;
import com.bossymr.network.SubscriptionEntity;
import com.bossymr.network.SubscriptionPriority;
import com.bossymr.network.client.NetworkEngine;
import com.bossymr.rapid.robot.network.robotware.RobotWareService;
import com.bossymr.rapid.robot.network.robotware.mastership.CloseableMastership;
import com.bossymr.rapid.robot.network.robotware.mastership.MastershipType;
import com.bossymr.rapid.robot.network.robotware.rapid.RapidService;
import com.bossymr.rapid.robot.network.robotware.rapid.execution.*;
import com.bossymr.rapid.robot.network.robotware.rapid.symbol.*;
import com.bossymr.rapid.robot.network.robotware.rapid.task.Task;
import com.bossymr.rapid.robot.network.robotware.rapid.task.TaskService;
import com.bossymr.rapid.robot.network.robotware.rapid.task.module.Module;
import com.bossymr.rapid.robot.network.robotware.rapid.task.module.*;
import com.bossymr.rapid.robot.network.robotware.rapid.task.program.Program;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@EnabledIf("com.bossymr.rapid.robot.network.NetworkTestUtil#doNetworkTest")
class RobotServiceTest {

    private static RobotService robotService;

    @BeforeAll
    static void beforeAll() {
        robotService = new NetworkEngine(NetworkTestUtil.DEFAULT_PATH, () -> NetworkTestUtil.DEFAULT_CREDENTIALS)
                .createService(RobotService.class);
    }

    private void assertsFailsWithCode(int expected, Executable executable) {
        try {
            executable.execute();
        } catch (ResponseStatusException e) {
            if (e.getResponse().statusCode() != expected) {
                fail();
            }
        } catch (Throwable e) {
            fail();
        }
    }

    @Test
    void getExecution() throws IOException, InterruptedException {
        RapidService rapidService = robotService.getRobotWareService().getRapidService();
        Task task = rapidService.getTaskService().getTask("T_ROB1").send();
        task.onExecutionState().subscribe(SubscriptionPriority.MEDIUM, (entity, event) -> {
            System.out.println(event.getState());
        }).join();
        Thread.sleep(60 * 1000);
    }

    @Test
    void stackFrame() throws IOException, InterruptedException {
        RapidService rapidService = robotService.getRobotWareService().getRapidService();
        Task task = rapidService.getTaskService().getTask("T_ROB1").send();
        assertsFailsWithCode(400, () -> task.getStackFrame(0).send());
        assertsFailsWithCode(400, () -> task.getStackFrame(1).send());
        ExecutionService executionService = rapidService.getExecutionService();
        executionService.resetProgramPointer().send();
        executionService.start(RegainMode.REGAIN, ExecutionMode.STEP_IN, ExecutionCycle.ONCE, ConditionState.NONE, BreakpointMode.DISABLED, TaskExecutionMode.NORMAL).send();
        assertsFailsWithCode(400, () -> task.getStackFrame(0).send());
        System.out.println(task.getStackFrame(1).send());
        System.out.println(task.getStackFrame(2).send());
        assertsFailsWithCode(400, () -> task.getStackFrame(3).send());
        SymbolQuery symbolQuery = new SymbolQuery()
                .setMethod(SymbolSearchMethod.BLOCK)
                .setBlock("RAPID/T_ROB1/StartModul")
                .setRecursive(true)
                .setName("level");
        List<SymbolModel> symbolModels = rapidService.findSymbols(symbolQuery).send();
        QueryableSymbol symbolState = (QueryableSymbol) symbolModels.get(0);
        System.out.println(symbolState.getValue().send());
        SymbolQuery symbolQuery1 = new SymbolQuery()
                .setRecursive(true)
                .setMethod(SymbolSearchMethod.BLOCK)
                .setSymbolType(SymbolType.RECORD_COMPONENT);
        System.out.println(rapidService.findSymbols(symbolQuery1).send());
    }

    @Test
    void start() {
        ExecutionService executionService = robotService.getRobotWareService().getRapidService().getExecutionService();
        robotService.getRobotWareService().getMastershipService().getDomain(MastershipType.RAPID).sendAsync()
                .thenComposeAsync(domain -> domain.request().sendAsync()
                        .thenComposeAsync(ignored -> executionService.resetProgramPointer().sendAsync())
                        .thenComposeAsync(ignored -> executionService.start(RegainMode.REGAIN, ExecutionMode.CONTINUE, ExecutionCycle.ONCE, ConditionState.CALLCHAIN, BreakpointMode.ENABLED, TaskExecutionMode.NORMAL).sendAsync())
                        .handleAsync((ignored, throwable) -> {
                            domain.release().sendAsync();
                            if (throwable instanceof RuntimeException) {
                                throw ((RuntimeException) throwable);
                            } else if (throwable != null) {
                                throw new CompletionException(throwable);
                            }
                            return null;
                        })).join();
    }

    @Test
    void removeBreakpoint() throws IOException, InterruptedException {
        Task task = robotService.getRobotWareService().getRapidService().getTaskService().getTask("T_ROB1").send();
        Program program = task.getProgram().send();
        program.setBreakpoint("StartModul", 12, 0).send();
        assertEquals(1, program.getBreakpoints().send().size());
        List<ModuleInfo> moduleInfos = task.getModules().send();
        Module module = moduleInfos.stream()
                .filter(moduleInfo -> moduleInfo.getName().equals("StartModul"))
                .findFirst().orElseThrow()
                .getModule().send();
        ModuleText moduleText = module.getText(12, 0, 12, 1).send();
        try (CloseableMastership ignored = CloseableMastership.request(robotService.getRobotWareService().getMastershipService())) {
            module.setText("T_ROB1", ReplaceMode.REPLACE, QueryMode.TRY, 12, 0, 12, 1, moduleText.getText()).send();
        }
        assertEquals(0, program.getBreakpoints().send().size());
    }

    @DisplayName("Test User Service")
    @Test
    void testUserService() throws Throwable {
        UserService userService = robotService.getUserService();
        userService.getGrants().send();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        CompletableFuture<SubscriptionEntity> subscriptionEntity = userService.getRemoteUserService().onRequest()
                .subscribe(SubscriptionPriority.MEDIUM, (entity, event) -> countDownLatch.countDown());
        SubscriptionEntity entity = subscriptionEntity.join();
        userService.getRemoteUserService().login().send();
        assertTrue(countDownLatch.await(5000, TimeUnit.MILLISECONDS));
        entity.unsubscribe().join();
    }

    @DisplayName("Test Controller Service")
    @Test
    void testControllerService() throws Throwable {
        Identity identity = robotService.getControllerService().getIdentity().send();
        assertNotNull(identity.getName());
    }

    @DisplayName("Test Task Service")
    @Test
    void testTaskService(@TempDir @NotNull File tempDirectory) throws Throwable {
        RobotWareService robotWareService = robotService.getRobotWareService();
        TaskService taskService = robotWareService.getRapidService().getTaskService();
        List<Task> tasks = taskService.getTasks().send();
        Task task = tasks.get(0);
        assertEquals("rap-task-li", task.getType());
        assertNotNull(task.getName());
        assertEquals("rap-task-li", task.getType());
        List<ModuleInfo> moduleInfos = task.getModules().send();
        long modules = moduleInfos.stream()
                .filter(moduleInfo -> moduleInfo.getModuleType().equals(ModuleType.PROGRAM_MODULE))
                .map(ModuleInfo::getName)
                .count();
        // The complete (non-list) module is automatically retrieved if a field or link is requested, which it does
        // not contain.
        assertEquals("rap-task", task.getType());
        Program program = task.getProgram().send();
        program.save(tempDirectory.getPath()).send();
        String[] strings = tempDirectory.list();
        assertNotNull(strings);
        // One of the saved files is a program file, containing an index of all modules in the program.
        assertEquals(modules + 1, strings.length);
        ModuleInfo moduleInfo = moduleInfos.get(0);
        moduleInfo.getModule().send();
    }
}