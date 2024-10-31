package com.bossymr.rapid;

import com.bossymr.rapid.robot.RobotService;
import com.bossymr.rapid.robot.api.GenericType;
import com.bossymr.rapid.robot.api.NetworkTarget;
import com.bossymr.rapid.robot.api.NetworkType;
import com.bossymr.rapid.robot.api.client.NetworkClient;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.URI;

@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(RobotTest.RobotAvailabilityCondition.class)
public @interface RobotTest {
    class RobotAvailabilityCondition implements ExecutionCondition {
        @Override
        public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
            NetworkClient client = new NetworkClient(URI.create("http://localhost"), RobotService.DEFAULT_CREDENTIALS);
            NetworkTarget<Void> target = NetworkTarget.newTarget(URI.create("/"), NetworkType.voidType())
                    .build();
            try {
                client.send(target);
            } catch (IOException | InterruptedException e) {
                return ConditionEvaluationResult.disabled("robot is not reachable");
            }
            return ConditionEvaluationResult.enabled("robot is reachable");
        }
    }

}
