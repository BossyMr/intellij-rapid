package com.bossymr.rapid.robot.network;

import com.bossymr.rapid.robot.network.query.Query;
import com.intellij.credentialStore.Credentials;
import junit.framework.TestCase;

import java.net.URI;
import java.util.List;
import java.util.Map;

public class RobotServiceTest extends TestCase {

    public void testConnect() throws Throwable {
        Credentials credentials = new Credentials("Default User", "robotics".toCharArray());
        RobotService robotService = RobotService.connect(URI.create("http://localhost:80"), credentials);
        // System.out.println(userService.getManualModePrivilegeService().getStatus().send());
        /*
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
         */
        // userService.getManualModePrivilegeService().request(RequestManualModePrivilege.MODIFY).send();
        // Thread.sleep(2000);
        // entity.unsubscribe();
        Map<String, String> map = new SymbolQueryBuilder().setRecursive(true).build();
        Query<List<SymbolState>> query = robotService.getRobotWareService().getRapidService().findSymbols(map);
        List<SymbolState> symbols = query.send();
        System.out.println(symbols.size());

        SymbolState symbolState = robotService.getRobotWareService().getRapidService().findSymbol("RAPID/T_ROB1/DrawModule/index").send();
        System.out.println(symbolState);
    }
}