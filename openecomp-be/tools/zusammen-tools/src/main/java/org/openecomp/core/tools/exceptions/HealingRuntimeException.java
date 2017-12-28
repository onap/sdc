package org.openecomp.core.tools.exceptions;

public class HealingRuntimeException extends RuntimeException {
    public HealingRuntimeException(String message, Exception exception) {
        super(message, exception);
    }

    public HealingRuntimeException(Exception exception) {
        super(exception);
    }
}
