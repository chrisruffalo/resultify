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

    T getThrows() throws Exception;

}
