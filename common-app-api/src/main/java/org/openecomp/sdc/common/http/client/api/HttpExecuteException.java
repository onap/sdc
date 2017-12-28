package org.openecomp.sdc.common.http.client.api;

public class HttpExecuteException extends Exception {

    private static final long serialVersionUID = 1L;

    public HttpExecuteException(String message) {
        super (message);
    }

    public HttpExecuteException(String message, Throwable cause) {
        super (message, cause);
    }

    public HttpExecuteException(Throwable cause) {
        super(cause);
    }
    
    @Override
    public Throwable getCause() {
        Throwable cause = super.getCause();
        return cause != null ? cause : this;
    }
}
