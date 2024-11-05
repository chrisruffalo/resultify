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

    /**
     * 
     *
     * @param output
     * @param error
     */
    ResultImpl(OUTPUT output, Exception error) {
        // an error cannot be present if there is already a value
        this.errorPresent = Optional.ofNullable(error).isPresent();
        if (this.errorPresent) {
            this.currentError = error;
            this.currentOutput = null;
            this.outputPresent = false;
        } else {
            this.outputPresent = Optional.ofNullable(output).isPresent();
            this.currentError = null;
            if (this.outputPresent) {
                this.currentOutput = output;
            } else {
                this.currentOutput = null;
            }
        }

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
