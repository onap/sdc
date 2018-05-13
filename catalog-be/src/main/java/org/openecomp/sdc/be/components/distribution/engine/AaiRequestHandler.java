package org.openecomp.sdc.be.components.distribution.engine;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.Properties;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AaiRequestHandler {

    private static final Logger logger = LoggerFactory.getLogger(AaiRequestHandler.class);
    private ExternalServiceConfig aaiConfig;
    
    protected static final String OPERATIONAL_ENV_RESOURCE_CONFIG_PARAM = "operationalEnvironments";
    protected static final String OPERATIONAL_ENV_RESOURCE = "/operational-environment";

    @PostConstruct
    public void init() {
        logger.debug("AaiRequestHandler has been initialized.");

        aaiConfig = ConfigurationManager.getConfigurationManager().getDistributionEngineConfiguration().getAaiConfig();
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
        }
        catch (Exception e) {
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
