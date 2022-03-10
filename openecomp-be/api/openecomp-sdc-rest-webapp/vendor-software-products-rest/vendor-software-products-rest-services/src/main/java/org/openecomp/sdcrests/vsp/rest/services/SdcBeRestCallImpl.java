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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.Nullable;
import org.onap.sdc.tosca.services.YamlUtil;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.http.client.api.HttpRequest;
import org.openecomp.sdc.common.http.client.api.HttpResponse;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdcrests.item.rest.services.catalog.notification.EntryNotConfiguredException;
import org.openecomp.sdcrests.item.rest.services.catalog.notification.http.HttpConfiguration;
import org.openecomp.sdcrests.vsp.rest.SdcBeRestCall;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;

import static javax.ws.rs.core.HttpHeaders.ACCEPT;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

public class SdcBeRestCallImpl implements SdcBeRestCall {

    private static final Logger LOGGER = LoggerFactory.getLogger(SdcBeRestCallImpl.class);
    private static final String URL_GET_RESOURCE_BY_CSAR_UUID = "%s://%s:%s/sdc2/rest/v1/catalog/resources/csar/%s";
    private static final String CONFIG_FILE_PROPERTY = "configuration.yaml";
    private static final String CONFIG_SECTION = "catalogNotificationsConfig";
    public static final String NAME = "name";

    public SdcBeRestCallImpl() { }

    @Override
    public Optional<String> getNameOfVfUsingVsp(String vspId, String user) throws Exception {
        try {
            HttpConfiguration httpConfig = getHttpConfiguration();
            if (null == httpConfig) {
                throw new Exception();
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
            if (respObject.containsKey(NAME)) {
                return Optional.of((String) respObject.get(NAME));
            }
            else {
                return Optional.empty();
            }
        } catch (Exception e) {
            LOGGER.warn("Error on rest call. ", e);
            throw e;
        }
    }

    @Nullable
    private HttpConfiguration getHttpConfiguration() throws Exception{
        HttpConfiguration httpConfig;
        try {
            String file = Objects.requireNonNull(System.getProperty(CONFIG_FILE_PROPERTY),
                    "Config file location must be specified via system property " + CONFIG_FILE_PROPERTY);
            Object config = getEndpointConfiguration(file);
            ObjectMapper mapper = new ObjectMapper();
            httpConfig = mapper.convertValue(config, HttpConfiguration.class);
        } catch (Exception e) {
            LOGGER.warn("Failed to load configuration. ", e);
            throw e;
        }
        return httpConfig;
    }

    private static Object getEndpointConfiguration(String file) throws IOException {
        Map<?, ?> configuration = Objects.requireNonNull(readConfigurationFile(file), "Configuration cannot be empty");
        Object endpointConfig = configuration.get(CONFIG_SECTION);
        if (endpointConfig == null) {
            throw new EntryNotConfiguredException(CONFIG_SECTION + " section");
        }
        return endpointConfig;
    }

    private static Map<?, ?> readConfigurationFile(String file) throws IOException {
        try (InputStream fileInput = new FileInputStream(file)) {
            YamlUtil yamlUtil = new YamlUtil();
            return yamlUtil.yamlToMap(fileInput);
        }
    }
}
