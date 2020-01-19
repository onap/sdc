/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.be.components.distribution.engine;

import org.apache.http.conn.ConnectTimeoutException;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.datastructure.FunctionalInterfaces;
import org.openecomp.sdc.common.datastructure.FunctionalInterfaces.SupplierThrows;
import org.openecomp.sdc.common.http.client.api.HttpExecuteException;
import org.openecomp.sdc.common.http.client.api.HttpRequest;
import org.openecomp.sdc.common.http.client.api.HttpResponse;
import org.openecomp.sdc.common.http.client.api.Responses;
import org.openecomp.sdc.common.http.config.ExternalServiceConfig;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.Properties;
import java.util.UUID;

@Component
public class AaiRequestHandler {

    private static final Logger logger = Logger.getLogger(AaiRequestHandler.class);
    private ExternalServiceConfig aaiConfig;

    protected static final String OPERATIONAL_ENV_RESOURCE_CONFIG_PARAM = "operationalEnvironments";
    protected static final String OPERATIONAL_ENV_RESOURCE = "/operational-environment";

    @PostConstruct
    public void init() {
        logger.debug("AaiRequestHandler has been initialized.");

        aaiConfig = ConfigurationManager.getConfigurationManager().getDistributionEngineConfiguration().getAaiConfig();
        aaiConfig.getHttpClientConfig().setEnableMetricLogging(true);
        logger.debug("AaiRequestHandler Configuration={}", aaiConfig);
    }


    public HttpResponse<String> getOperationalEnvById(String id) {
        Properties headers = createHeaders();
        String url = String.format("%s%s%s/%s",
                aaiConfig.getHttpRequestConfig().getServerRootUrl(),
                aaiConfig.getHttpRequestConfig().getResourceNamespaces().get(OPERATIONAL_ENV_RESOURCE_CONFIG_PARAM),
                OPERATIONAL_ENV_RESOURCE, id);

        SupplierThrows<HttpResponse<String>, Exception> httpGet = () -> HttpRequest.get(url, headers, aaiConfig.getHttpClientConfig());
        long maxRetries = aaiConfig.getHttpClientConfig().getNumOfRetries();
        try {
            return FunctionalInterfaces.retryMethodOnException(httpGet, this::retryOnException, maxRetries);

        } catch (Exception e) {
            logger.debug("Request failed with exception {}", getCause(e).getMessage());
            return Responses.INTERNAL_SERVER_ERROR;
        }
    }


    

    private boolean retryOnException(Exception e) {
        Throwable cause = getCause(e);
        return !(cause instanceof ConnectTimeoutException || cause instanceof ConnectException || cause instanceof SocketTimeoutException);
    }


    private Throwable getCause(Exception e) {
        if (e instanceof HttpExecuteException) {
            return e.getCause();
        }
        return e;
    }
    
    
    private Properties createHeaders() {
        Properties headers = new Properties();
        headers.put(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
        headers.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        headers.put(Constants.X_TRANSACTION_ID_HEADER, UUID.randomUUID().toString());

        return headers;
    }
}
