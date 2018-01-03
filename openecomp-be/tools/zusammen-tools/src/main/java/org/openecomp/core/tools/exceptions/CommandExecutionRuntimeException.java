package org.openecomp.core.tools.exceptions;

public class CommandExecutionRuntimeException extends RuntimeException {
    public CommandExecutionRuntimeException(String message, Exception exception) {
        super(message, exception);
    }

    public CommandExecutionRuntimeException(String message) {
        super(message);
    }
}
