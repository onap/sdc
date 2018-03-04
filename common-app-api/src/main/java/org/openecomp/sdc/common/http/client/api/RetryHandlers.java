package org.openecomp.sdc.common.http.client.api;

import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class RetryHandlers {

    private static final Logger LOGGER = LoggerFactory.getLogger(RetryHandlers.class);

    private RetryHandlers(){}

    public static ComparableHttpRequestRetryHandler getDefault(int numOfRetries) {
        return (IOException exception, int executionCount, HttpContext context) -> {
            LOGGER.debug("failed sending request with exception", exception);
            LOGGER.debug("try request number: {}", executionCount);
            return executionCount <= numOfRetries;
        };
    }


}
