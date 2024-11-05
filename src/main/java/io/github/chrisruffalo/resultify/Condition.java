package io.github.chrisruffalo.resultify;

import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;

/**
 * A condition is for a `recover` method to continue
 * trying recovery until some condition is met. Once
 * no error is encountered the condition will no longer
 * be checked so `indefinitely` just means "never stop trying
 * until you get a non-error result".
 *
 * @param <OUTPUT>
 */
@FunctionalInterface
public interface Condition<OUTPUT> {

    boolean met(Result<OUTPUT> currentResult);

    /**
     * When used as a recovery condition it will cause
     * recovery to be repeated until a non-error result
     * occurs. Keep in mind that this will keep failing
     * code looping forever.
     *
     * @return a condition that allows recovery to repeat indefinitely
     * @param <OUTPUT> result value type
     */
    static <OUTPUT> Condition<OUTPUT> indefinitely() {
        return result -> false;
    }

    /**
     * When used as a condition causes the result to be checked
     * a max number of times before discontinuing recovery. As
     * always recovery is still finished when a non-error result
     * is returned.
     *
     * @param maxTimes number of times to re-try recovery
     * @return a condition that allows recovery a maximum number of times
     * @param <OUTPUT> result value type
     */
    static <OUTPUT> Condition<OUTPUT> atMost(int maxTimes) {
        return new Condition<OUTPUT>() {
            int times = 0;

            @Override
            public boolean met(Result<OUTPUT> currentResult) {
                times++;
                return times > maxTimes;
            }
        };
    }

    /**
     * A condition that allows recovery to repeat for some time period. Keep
     * in mind that there is no built-in delay between recovery attempts. As
     * always recovery will end if no error is encountered.
     *
     * @param duration to await for recovery
     * @return a condition that allows recovery to repeat for some time period
     * @param <OUTPUT> result value type
     */
    static <OUTPUT> Condition<OUTPUT> forDuration(Duration duration) {
        final Instant maxTime = Instant.now().plus(duration.toMillis(), ChronoUnit.MILLIS);
        return result -> Instant.now().isAfter(maxTime);
    }
}
