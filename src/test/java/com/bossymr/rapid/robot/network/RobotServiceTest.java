package com.bossymr.rapid.robot.network;

import com.bossymr.network.SubscriptionEntity;
import com.bossymr.network.SubscriptionPriority;
import com.bossymr.network.client.NetworkEngine;
import com.bossymr.rapid.robot.network.robotware.RobotWareService;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.util.List;
import java.util.concurrent.CompletableFuture;
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

    @DisplayName("Test User Service")
    @Test
    void testUserService() throws Throwable {
        UserService userService = robotService.getUserService();
        userService.getGrants().send();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        CompletableFuture<SubscriptionEntity> subscriptionEntity = userService.getRemoteUserService().onRequest()
                .subscribe(SubscriptionPriority.MEDIUM, (entity, event) -> countDownLatch.countDown());
        userService.getRemoteUserService().login().send();
        SubscriptionEntity entity = subscriptionEntity.join();
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