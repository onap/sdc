package org.openecomp.sdc.be.components.validation;

import org.openecomp.sdc.exception.ResponseFormat;

/**
 * Created by chaya on 10/18/2017.
 */
public class ValidationException extends RuntimeException {

    private transient ResponseFormat exceptionResponseFormat;

    public ValidationException(ResponseFormat exceptionResponseFormat) {
        super();
        this.exceptionResponseFormat = exceptionResponseFormat;
    }

    public ResponseFormat getExceptionResponseFormat() {
        return exceptionResponseFormat;
    }

}
