package io.github.chrisruffalo.resultify;

import java.util.function.Function;

/**
 * Interface to allow a function with an exception. Inspired
 * by <a href="https://stackoverflow.com/a/27252163">this SO post</a>.
 *
 * @param <T> input type
 * @param <R> return type
 */
@FunctionalInterface
public interface ThrowingFunction<T, R> extends Function<T, R> {

    @Override
    default R apply(final T elem) {
        try {
            return applyThrows(elem);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Apply a function to a funtion with a throwable element
     *
     * @param elem to throw
     * @return value
     * @throws Exception when thrown
     */
    R applyThrows(T elem) throws Exception;

}
