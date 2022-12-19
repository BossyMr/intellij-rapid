package com.bossymr.rapid.robot.network;

import com.bossymr.rapid.robot.ResponseStatusException;
import com.bossymr.rapid.robot.impl.RemoteServiceImpl;
import com.bossymr.rapid.robot.network.query.Query;
import com.intellij.credentialStore.Credentials;
import com.intellij.testFramework.LightIdeaTestCase;
import org.junit.Assert;

import java.net.URI;
import java.util.List;
import java.util.Map;

public class RobotServiceTest extends LightIdeaTestCase {

    public void testService() throws Throwable {
        RemoteServiceImpl service = new RemoteServiceImpl();
        service.connect(URI.create("http://localhost:80"), new Credentials("Default User", "robotics".toCharArray()));
        System.out.println(service.getRobotState());
    }

    public void testConnect() throws Throwable {
        Credentials credentials = new Credentials("Default User", "robotics".toCharArray());
        RobotService robotService = RobotService.connect(URI.create("http://localhost:80"), credentials);
        Map<String, String> map = new SymbolQueryBuilder().setRecursive(true).build();
        Query<List<SymbolState>> query = robotService.getRobotWareService().getRapidService().findSymbols(map);
        List<SymbolState> symbols = query.send();
        System.out.println(symbols.size());

        /*
        List<EventLogCategory> categories = robotService.getRobotWareService().getEventLogService().getCategories("en").send();
        for (EventLogCategory category : categories) {
            category.onMessage()
                    .subscribe(SubscriptionPriority.MEDIUM, (entity, event) -> {
                        System.out.println("Received event: " + event);
                        event.getMessage("en").sendAsync()
                                .thenAcceptAsync(response -> System.out.println("Fetched event: " + response));
                    });
        }

        System.out.println("Waiting");
        Thread.sleep(60000);
         */

        SymbolState symbolState = robotService.getRobotWareService().getRapidService().findSymbol("RAPID/T_ROB1/DrawModule/index").send();
        System.out.println(symbolState);
        Assert.assertNotNull(symbolState.getLink("self"));
        try {
            robotService.getRobotWareService().getRapidService().findSymbol("RAPID/T_ROB1/DrawModule/index2").send();
            fail();
        } catch (ResponseStatusException e) {
            Assert.assertEquals(400, e.getStatusCode());
        }
    }
}