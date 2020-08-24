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

package org.onap.sdc.backend.ci.tests.utils.rest;

import com.google.gson.Gson;

import java.util.List;

import org.onap.sdc.backend.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.be.model.User;
import org.onap.sdc.backend.ci.tests.api.Urls;
import org.onap.sdc.backend.ci.tests.config.Config;
import org.onap.sdc.backend.ci.tests.datatypes.ServiceFilterDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceFilterUtils extends BaseRestUtils {

    private static Logger logger = LoggerFactory.getLogger(ServiceFilterUtils.class.getName());

    private static Gson gson = new Gson();

    public static RestResponse createServiceFilter(String externalServiceId, String proxyServiceId,
                                                   ServiceFilterDetails serviceFilterDetails,
                                                   User user) throws Exception{
        Config config = Config.instance();

        String url = String.format(Urls.CREATE_SERVICE_FILTER, config.getCatalogBeHost(), config.getCatalogBePort(),
                externalServiceId, proxyServiceId);

        return sendPost(url, gson.toJson(serviceFilterDetails), user.getUserId(), acceptHeaderData);
    }

    public static RestResponse updateServiceFilter(String externalServiceId, String proxyServiceId,
                                                   List<ServiceFilterDetails> serviceFilterDetailsList,
                                                   User user) throws Exception{
        Config config = Config.instance();

        String url = String.format(Urls.UPDATE_SERVICE_FILTER, config.getCatalogBeHost(), config.getCatalogBePort(),
                externalServiceId, proxyServiceId);

        return sendPut(url, gson.toJson(serviceFilterDetailsList), user.getUserId(), acceptHeaderData);
    }

    public static RestResponse deleteServiceFilter(String externalServiceId, String proxyServiceId,
                                                   int constraintIndex,
                                                   User user) throws Exception{
        Config config = Config.instance();

        String url = String.format(Urls.DELETE_SERVICE_FILTER, config.getCatalogBeHost(), config.getCatalogBePort(),
                externalServiceId, proxyServiceId, constraintIndex);

        return sendDelete(url, user.getUserId());
    }
}
