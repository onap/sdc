package org.openecomp.core.tools.exceptions;

public class AddContributorRuntimeException extends RuntimeException {
    public AddContributorRuntimeException(String message, Exception exception) {
        super(message, exception);
    }

    public AddContributorRuntimeException(String message) {
        super(message);
    }
}
