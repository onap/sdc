package org.openecomp.sdc.be.components.validation;

import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.exception.ResponseFormat;

public class ValidationUtils {

    private static final Logger log = Logger.getLogger(ValidationUtils.class);

    public static  <T> T throwValidationException(ResponseFormat responseFormat, String logMessage, Object ... logParams){
        log.error(logMessage, logParams);
        throw new ValidationException(responseFormat);
    }

}
