package com.bossymr.rapid.robot.network.query;

import java.io.IOException;

public interface SubscriptionEntity {

    void unsubscribe() throws IOException, InterruptedException;

}
