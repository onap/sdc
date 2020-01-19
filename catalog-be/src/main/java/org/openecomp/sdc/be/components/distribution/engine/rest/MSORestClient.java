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

package org.openecomp.sdc.be.components.distribution.engine.rest;

import com.google.common.annotations.VisibleForTesting;
import com.google.gson.Gson;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.eclipse.jetty.util.URIUtil;
import org.openecomp.sdc.be.components.distribution.engine.DistributionStatusNotificationEnum;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.common.http.client.api.*;
import org.openecomp.sdc.common.http.config.BasicAuthorization;
import org.openecomp.sdc.common.http.config.ExternalServiceConfig;
import org.openecomp.sdc.common.http.config.HttpClientConfig;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component
public class MSORestClient {

    private static final Logger logger = Logger.getLogger(MSORestClient.class.getName());
    private static final Gson gson = new Gson();
    @VisibleForTesting
    static final String DISTRIBUTIONS_RESOURCE_CONFIG_PARAM = "distributions";

    private ExternalServiceConfig serviceConfig = ConfigurationManager.getConfigurationManager().getDistributionEngineConfiguration().getMsoConfig();

    public MSORestClient() {
        HttpClientConfig httpClientConfig = serviceConfig.getHttpClientConfig();
        int numOfRetries = httpClientConfig.getNumOfRetries(); 
        if ( numOfRetries > 0 ) {
            httpClientConfig.setRetryHandler(RetryHandlers.getDefault(numOfRetries));
        }
        serviceConfig.getHttpClientConfig().setEnableMetricLogging(true);
    }

    public HttpResponse<String> notifyDistributionComplete(String distributionId, DistributionStatusNotificationEnum distributionStatusEnum, String errReason) {
        try {
            return doNotifyDistributionComplete(distributionId, distributionStatusEnum, errReason);
        }
        catch(HttpExecuteException e) {
            logger.debug("The request to mso failed with exception ", e);
            return Responses.INTERNAL_SERVER_ERROR;
        }
    }

    private HttpResponse<String> doNotifyDistributionComplete(String distributionId, DistributionStatusNotificationEnum distributionStatusEnum, String errReason) throws HttpExecuteException {
        StringEntity entity = new StringEntity(gson.toJson(new DistributionStatusRequest(distributionStatusEnum.name(), errReason)), ContentType.APPLICATION_JSON);
        HttpResponse<String> response = HttpRequest.patch(buildMsoDistributionUrl(distributionId), buildReqHeader(), entity, serviceConfig.getHttpClientConfig());
        return response;
    }

    private Properties buildReqHeader() {
        Properties properties = new Properties();
        BasicAuthorization basicAuth = serviceConfig.getHttpClientConfig().getBasicAuthorization();
        RestUtils.addBasicAuthHeader(properties, basicAuth.getUserName(), basicAuth.getPassword());
        return properties;
    }

    private String buildMsoDistributionUrl(String distributionId) {
        String msoBaseUrl = serviceConfig.getHttpRequestConfig().getServerRootUrl();
        String distributionsPath = serviceConfig.getHttpRequestConfig().getResourceNamespaces().get(DISTRIBUTIONS_RESOURCE_CONFIG_PARAM);
        String distributionsApiPath = distributionsPath + URIUtil.SLASH + distributionId;
        return msoBaseUrl + distributionsApiPath;
    }

}
