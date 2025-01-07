package io.github.chrisruffalo.resultify;

import java.util.function.Supplier;

/**
 * Interface to allow a supplier with an exception. Inspired
 * by <a href="https://stackoverflow.com/a/27252163">this SO post</a>.
 *
 * @param <T> input type
 */
@FunctionalInterface
public interface ThrowingSupplier<T> extends Supplier<T> {

    /**
     * Supply value
     *
     * @return the value from the supplier
     */
    default T get() {
        try {
            return this.getThrows();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * The value to get when the thrown function might occur
     *
     * @return value to get
     * @throws Exception when a throwable exception happens
     */
    T getThrows() throws Exception;

}
