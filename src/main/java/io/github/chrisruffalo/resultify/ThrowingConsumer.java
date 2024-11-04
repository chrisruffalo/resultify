package io.github.chrisruffalo.resultify;

import java.util.function.Consumer;

/**
 * Interface to allow a consumer with an exception. Inspired
 * by <a href="https://stackoverflow.com/a/27252163">this SO post</a>.
 *
 * @param <T> input type
 */
@FunctionalInterface
public interface ThrowingConsumer<T> extends Consumer<T> {

    /**
     * Performs this operation on the given argument.
     *
     * @param t the input argument
     */
    default void accept(T t) {
        try {
            this.acceptThrows(t);
        } catch (Exception ex) {
            // no-op
        }
    }

    void acceptThrows(T t) throws Exception;

}
