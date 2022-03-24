/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2022 Nordix Foundation
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 *
 *
 */

package org.openecomp.sdcrests.vsp.rest.services;

import static javax.ws.rs.core.HttpHeaders.ACCEPT;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import org.jetbrains.annotations.Nullable;
import org.openecomp.core.validation.errors.ErrorMessagesFormatBuilder;
import org.openecomp.sdc.common.CommonConfigurationManager;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.errors.CatalogRestClientException;
import org.openecomp.sdc.common.errors.Messages;
import org.openecomp.sdc.common.http.client.api.HttpRequest;
import org.openecomp.sdc.common.http.client.api.HttpResponse;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdcrests.item.rest.services.catalog.notification.http.HttpConfiguration;
import org.openecomp.sdcrests.vsp.rest.CatalogVspClient;

public class CatalogVspClientImpl implements CatalogVspClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(CatalogVspClientImpl.class);
    private static final String URL_GET_RESOURCE_BY_CSAR_UUID = "%s://%s:%s/sdc2/rest/v1/catalog/resources/csar/%s";
    private static final String CONFIG_SECTION = "catalogNotificationsConfig";
    public static final String NAME = "name";
    public static final String SDC_2_REST_V_1_CATALOG_RESOURCES_CSAR_CSARUUID = "sdc2/rest/v1/catalog/resources/csar/{csaruuid}";

    /**
     * Returns the name of a VF which is using the provided VSP.
     * It returns an empty optional in case the VSP is not used by any VF,
     * or throws ans exception if any error occurs during the process.
     *
     * @param vspId        the id of the vsp
     * @param user         the user to perform the action
     */
    @Override
    public Optional<String> findNameOfVfUsingVsp(String vspId, String user) throws CatalogRestClientException {
        try {
            HttpConfiguration httpConfig = getHttpConfiguration();
            if (null == httpConfig) {
                throw new CatalogRestClientException(ErrorMessagesFormatBuilder.getErrorWithParameters(Messages.DELETE_VSP_UNEXPECTED_ERROR_USED_BY_VF.getErrorMessage(),
                        vspId, SDC_2_REST_V_1_CATALOG_RESOURCES_CSAR_CSARUUID));
            }
            final Properties headers = new Properties();
            headers.put(Constants.USER_ID_HEADER, user);
            headers.put(ACCEPT, APPLICATION_JSON);
            String url = String.format(URL_GET_RESOURCE_BY_CSAR_UUID, httpConfig.getCatalogBeProtocol(),
                    httpConfig.getCatalogBeFqdn(), httpConfig.getCatalogBeHttpPort(), vspId);
            final HttpResponse<String> httpResponse;
            httpResponse = HttpRequest.get(url, headers);
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> respObject = mapper.readValue(httpResponse.getResponse(), Map.class);
            return Optional.of((String) respObject.get(NAME));

        } catch (Exception e) {
            String formattedErrorMessage = ErrorMessagesFormatBuilder.getErrorWithParameters(Messages.DELETE_VSP_UNEXPECTED_ERROR_USED_BY_VF.getErrorMessage(),
                    vspId, SDC_2_REST_V_1_CATALOG_RESOURCES_CSAR_CSARUUID);
            LOGGER.error(formattedErrorMessage,  e);
            throw new CatalogRestClientException(formattedErrorMessage, e);
        }
    }

    @Nullable
    private HttpConfiguration getHttpConfiguration() throws CatalogRestClientException {
        HttpConfiguration httpConfig;
        try {
            Object config = getEndpointConfiguration();
            ObjectMapper mapper = new ObjectMapper();
            httpConfig = mapper.convertValue(config, HttpConfiguration.class);
        } catch (Exception e) {
            LOGGER.error("Failed to load configuration. ", e);
            throw new CatalogRestClientException("Failed to load configuration. ", e);
        }
        return httpConfig;
    }

    private static Object getEndpointConfiguration() {
        final var commonConfigurationManager = CommonConfigurationManager.getInstance();
        return commonConfigurationManager.getConfigValue(CONFIG_SECTION);
    }
}
