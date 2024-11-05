package io.github.chrisruffalo.resultify;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

class ConditionTest {

    @Test
    void indefinitely() {
        final Condition<String> indef = Condition.indefinitely();
        Assertions.assertFalse(indef.met(null));
        Assertions.assertFalse(indef.met(null));
        Assertions.assertFalse(indef.met(null));
    }

    @Test
    void atMost() {
        final Condition<String> atMost = Condition.atMost(3);
        Assertions.assertFalse(atMost.met(null));
        Assertions.assertFalse(atMost.met(null));
        Assertions.assertFalse(atMost.met(null));
        Assertions.assertTrue(atMost.met(null));
    }

    @Test
    void forDuration() throws InterruptedException {
        final Condition<String> duration = Condition.forDuration(Duration.of(300, ChronoUnit.MILLIS));
        Assertions.assertFalse(duration.met(null));
        Thread.sleep(100);
        Assertions.assertFalse(duration.met(null));
        Thread.sleep(210);
        Assertions.assertTrue(duration.met(null));
    }

}