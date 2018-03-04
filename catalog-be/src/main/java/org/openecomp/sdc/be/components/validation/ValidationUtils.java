package org.openecomp.sdc.be.components.validation;

import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ValidationUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ValidationUtils.class);

    public static  <T> T throwValidationException(ResponseFormat responseFormat, String logMessage, Object ... logParams){
        LOGGER.error(logMessage, logParams);
        throw new ValidationException(responseFormat);
    }

}
