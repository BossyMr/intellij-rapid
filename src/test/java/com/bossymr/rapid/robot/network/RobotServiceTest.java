package com.bossymr.rapid.robot.network;

import com.bossymr.network.SubscriptionEntity;
import com.bossymr.network.SubscriptionPriority;
import com.bossymr.network.client.NetworkEngine;
import com.bossymr.network.client.NetworkManager;
import com.bossymr.rapid.robot.network.robotware.RobotWareService;
import com.bossymr.rapid.robot.network.robotware.rapid.task.Task;
import com.bossymr.rapid.robot.network.robotware.rapid.task.TaskService;
import com.bossymr.rapid.robot.network.robotware.rapid.task.module.ModuleInfo;
import com.bossymr.rapid.robot.network.robotware.rapid.task.module.ModuleType;
import com.bossymr.rapid.robot.network.robotware.rapid.task.program.Program;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
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
        robotService = NetworkManager.newBuilder(NetworkTestUtil.DEFAULT_PATH)
                .setCredentials(NetworkTestUtil.DEFAULT_CREDENTIALS).build()
                .createService(RobotService.class);
    }

    @DisplayName("Test User Service")
    @Test
    void testUserService() throws IOException, InterruptedException {
        UserService userService = robotService.getUserService();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        SubscriptionEntity subscriptionEntity = userService.getRemoteUserService().onRequest()
                .subscribe(SubscriptionPriority.MEDIUM, (entity, event) -> countDownLatch.countDown());
        userService.getRemoteUserService().login().get();
        assertTrue(countDownLatch.await(500, TimeUnit.MILLISECONDS));
        subscriptionEntity.unsubscribe();
    }

    @DisplayName("Test Controller Service")
    @Test
    void testControllerService() throws IOException, InterruptedException {
        Identity identity = robotService.getControllerService().getIdentity().get();
        assertNotNull(identity.getName());
    }
}