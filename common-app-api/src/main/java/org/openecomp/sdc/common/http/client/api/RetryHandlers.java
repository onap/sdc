package org.openecomp.sdc.common.http.client.api;

import org.apache.http.protocol.HttpContext;
import org.openecomp.sdc.common.log.wrappers.Logger;

import java.io.IOException;

public class RetryHandlers {

    private static final Logger logger = Logger.getLogger(RetryHandlers.class.getName());

    private RetryHandlers(){}

    public static ComparableHttpRequestRetryHandler getDefault(int numOfRetries) {
        return (IOException exception, int executionCount, HttpContext context) -> {
            logger.debug("failed sending request with exception", exception);
            logger.debug("try request number: {}", executionCount);
            return executionCount <= numOfRetries;
        };
    }


}
