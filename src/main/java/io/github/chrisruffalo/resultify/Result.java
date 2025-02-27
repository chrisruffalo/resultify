package io.github.chrisruffalo.resultify;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * A monad representing either an output type that should
 * be returned or an error that was thrown during the
 * attempt.
 *
 * @param <OUTPUT> type expected when operation completes
 */
public interface Result<OUTPUT> {

    /**
     * Get the output value of the called function. This value is only present when
     * no error was encountered.
     * 
     * @return output of the original called function
     */
    OUTPUT get();

    /**
     * Throws an exception if there is an error/exception in the result. Gets
     * the output value otherwise.
     *
     * @return the output value if no error is present, throws a runtime exception if an error is present
     * @throws RuntimeException exception if an error is present. if the present error is not already a runtime exception it will be wrapped
     */
    default OUTPUT panicOrGet() {
        if (isError()) {
            final Exception ex = error();
            if (ex instanceof RuntimeException) {
                throw (RuntimeException)ex;
            } else {
                throw new RuntimeException(ex);
            }
        }
        return get();
    }

    /**
     * A shortcut for {code}Result.failsafe(value).get(){/code}. The
     * failsafe value is only set if no value is present.
     *
     * @param failsafeValue to set
     * @return the value contained in the result or the failsafe value if not present
     */
    default OUTPUT getOrFailsafe(OUTPUT failsafeValue) {
        return failsafe(failsafeValue).get();
    }

    /**
     * Get the error that was encountered during the called function.
     *
     * @return error value encountered while calling the function
     */
    Exception error();

    /**
     * Converts the result to an optional allowing use of optional propcessing/lambdas.
     *
     * @return the output object represented as an optional
     */
    default Optional<OUTPUT> asOptional() {
        return Optional.ofNullable(get());
    }

    /**
     * True when a result value is present (no error, result is not empty)
     *
     * @return true when a result is present, false otherwise
     */
    boolean isPresent();

    /**
     * True when no result value is present (either an error or the result is empty)
     *
     * @return false when no result is present (either through error or no result given)
     */
    default boolean isEmpty() {
        return isError() || !isPresent();
    }

    /**
     * True when an error (checked or unchecked exception) is collected.
     *
     * @return true when an error is encountered, false otherwise
     */
    boolean isError();

    /**
     * Given a result, map it to another result.
     *
     * @param function mapping function
     * @return a new result after the map
     * @param <EXPECTED> the new type after mapping
     */
    default <EXPECTED> Result<EXPECTED> map(ThrowingFunction<OUTPUT, EXPECTED> function) {
        if (isError()) {
            return Result.of(null, this.error());
        }
        try {
            return Result.of(function.apply(this.get()), null);
        } catch (Exception ex) {
            return Result.of(null, ex);
        }
    }

    /**
     * Transform an error into a result (with output).
     *
     * @param with function to use for recovering
     * @return result with recovered output (or error if no recovery possible)
     */
    default Result<OUTPUT> recover(ThrowingFunction<Exception, OUTPUT> with) {
        if (!this.isError()) {
            return this;
        }
        try {
            return Result.of(with.apply(this.error()), null);
        } catch (Exception ex) {
            return Result.of(null, ex);
        }
    }

    /**
     * Recover until either the recovery produces no error or until some condition is met. If
     * there is no error during recovery then recovery ends. If there is an error the recovery
     * `with` function will be executed until the condition is met (or recovery is successful).
     *
     * @param with function to recover from failure (error) with
     * @param until some condition is met
     * @return result with recovered output (or error if no recovery possible)
     */
    default Result<OUTPUT> recover(ThrowingFunction<Exception, OUTPUT> with, Condition<OUTPUT> until) {
        Result<OUTPUT> result;
        do {
            result = this.recover(with);
        } while(result.isError() && !until.met(result));

        return result;
    }

    /**
     * Invoke the given consumer but do not expect a return value (the chain
     * continues within the same result context).
     *
     * @param invokeConsumer to invoke on the result
     * @return the current result without change
     */
    default Result<OUTPUT> invoke(ThrowingConsumer<Result<OUTPUT>> invokeConsumer) {
        invokeConsumer.accept(this);
        return this;
    }

    /**
     * Provide a result if there is no present result value. This allows
     * chaining of sources without involving looping logic. The supplier
     * will not be called if a value is already provided.
     *
     * @param supplier of the output value
     * @return a result with either the supplied value or an error
     */
    default Result<OUTPUT> provide(ThrowingSupplier<OUTPUT> supplier) {
        if (isPresent()) {
            return this;
        }

        try {
            return Result.of(supplier.get(), null);
        } catch (Exception ex) {
            return Result.of(null, ex);
        }
    }

    /**
     * If the value is still empty then provide a failsafe value
     *
     * @param value to use in the event that the result output is empty
     * @return the failsafe result
     */
    default Result<OUTPUT> failsafe(OUTPUT value) {
        if (isEmpty()) {
            return Result.of(value, null);
        }
        return this;
    }

    /**
     * If the value is present then the filter will be executed. The
     * filter should return TRUE if the value passes the filter (is
     * a good result) and FALSE if the value should not be the result. The
     * filter will only be called if a value is present. The filter MAY NOT
     * throw a (checked) exception.
     *
     * @param filter to use that will return TRUE if the value should be used as a result, false otherwise
     * @return a result with the value filtered out if it does not match the filter
     */
    default Result<OUTPUT> filter(Function<OUTPUT, Boolean> filter) {
        if (!isPresent()) {
            return this;
        }

        if (filter.apply(this.get())) {
            return this;
        }
        return Result.empty();
    }

    /**
     * Execute a runnable function if there is an output result
     * 
     * @param runnable to run if an output is present
     */
    default void ifPresent(Runnable runnable) {
        if(isPresent()) {
            runnable.run();
        }
    }

    /**
     * Execute a runnable if there is no result, either because
     * of an error or because no result was returned.
     * 
     * @param runnable to run if no output is present
     */
    default void ifEmpty(Runnable runnable) {
        if(isEmpty()) {
            runnable.run();
        }
    }

    /**
     * Execute a runnable if there is an error
     * 
     * @param runnable to run if an error occurred
     */
    default void ifError(Runnable runnable) {
        if (isError()) {
            runnable.run();
        }
    }

    /**
     * Given some function call the function and then
     * determine the result.
     *
     * @param function to call to get the result
     * @return a result monad that is either contains the result value or the error
     * @param <RESULT> value type
     */
    static <RESULT> Result<RESULT> from(Callable<RESULT> function) {
        try {
            return Result.of(function.call(), null);
        } catch (Exception e) {
            return Result.of(null, e);
        }
    }

    /**
     * Create a result from a value and exception and use the result logic
     * to determine if the result value is present or if the
     * error is present.
     *
     * @param result value to use as the result
     * @param ex exception to use as the error
     * @return a result monad with the result value or the exception if no result is present
     * @param <RESULT> value type
     */
    static <RESULT> Result<RESULT> of(RESULT result, Exception ex) {
        return new ResultImpl<>(result, ex);
    }

    /**
     * A shortcut for {code}Result.of(value, null){/code}.
     *
     * @param result value to use as the result
     * @return a result monad with the result value
     * @param <RESULT> value type
     */
    static <RESULT> Result<RESULT> of(RESULT result) {
        return new ResultImpl<>(result, null);
    }

    /**
     * Represents an empty result. No value and no error.
     *
     * @return an empty result
     * @param <RESULT> value type
     */
    static <RESULT> Result<RESULT> empty() {
        return Result.of(null, null);
    }

    /**
     * Given an array of callable methods to call return the first one that produces
     * an output. The methods/functions/callables will be called in order.
     * This method, however, cannot return a result with an error and so
     * any errors that are not logged are lost.
     *
     * @param functions to call, in order
     * @return the first result with no error, or an empty result
     * @param <RESULT> value type
     */
    @SafeVarargs
    static <RESULT> Result<RESULT> first(Callable<RESULT>... functions) {
        return first(Arrays.stream(functions).map(Result::from));
    }

    /**
     * Given a collection of callable methods to call return the first one that produces
     * an output. The methods/functions/callables will be called in order.
     * This method, however, cannot return a result with an error and so
     * any errors that are not logged are lost.
     *
     * @param functions to call, in order
     * @return the first result with no error, or an empty result
     * @param <RESULT> value type
     */
    static <RESULT> Result<RESULT> first(Collection<Callable<RESULT>> functions) {
        return first(functions.stream().map(Result::from));
    }

    /**
     * Shared code to convert a list of wrapped results into a single
     * result by finding the first present value.
     *
     * @param resultStream stream of wrapped results
     * @return the first present result in the stream
     * @param <RESULT> value type
     */
    private static <RESULT> Result<RESULT> first(Stream<Result<RESULT>> resultStream) {
        return resultStream.filter(Result::isPresent).findFirst().orElse(Result.empty());
    }


}
