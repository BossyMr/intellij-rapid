package com.bossymr.rapid.robot.network;

import com.bossymr.rapid.robot.network.query.Query;
import com.bossymr.rapid.robot.network.query.SubscriptionEntity;
import com.bossymr.rapid.robot.network.query.SubscriptionPriority;
import com.intellij.credentialStore.Credentials;
import junit.framework.TestCase;

import java.net.URI;

public class RobotServiceTest extends TestCase {

    public void testConnect() throws Throwable {
        Credentials credentials = new Credentials("Default User", "robotics".toCharArray());
        RobotService robotService = RobotService.connect(URI.create("http://localhost:80"), credentials);
        UserService userService = robotService.getUserService();
        userService.getGrants().send();
        System.out.println(userService.getManualModePrivilegeService().getStatus().send());
        SubscriptionEntity entity = userService.getManualModePrivilegeService().onRequest().subscribe(SubscriptionPriority.MEDIUM, (subscriptionEntity, poll) -> {
            System.out.println("Received: " + poll);
            Query<ManualModePrivilegeState> state = poll.getState();
            try {
                ManualModePrivilegeState result = state.send();
                System.out.println("Response: " + result);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        });
        userService.getManualModePrivilegeService().request(RequestManualModePrivilege.MODIFY).send();
        Thread.sleep(2000);
        entity.unsubscribe();
    }
}