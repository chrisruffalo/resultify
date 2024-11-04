package io.github.chrisruffalo.resultify;

import java.util.Optional;

/**
 * Implementation of result type.
 *
 * @param <OUTPUT> type expected when operation completes
 */
class ResultImpl<OUTPUT> implements Result<OUTPUT> {

    private final OUTPUT currentOutput;
    private final boolean outputPresent;

    private final Exception currentError;
    private final boolean errorPresent;

    public ResultImpl(OUTPUT output, Exception error) {
        this.currentOutput = output;
        this.currentError = error;
        this.outputPresent = Optional.ofNullable(output).isPresent();
        // an error cannot be present if there is already a value
        this.errorPresent = !outputPresent && Optional.ofNullable(error).isPresent();
    }

    @Override
    public OUTPUT get() {
        return currentOutput;
    }

    @Override
    public Exception error() {
        return currentError;
    }

    @Override
    public boolean isPresent() {
        return outputPresent;
    }

    @Override
    public boolean isError() {
        return errorPresent;
    }

}
