/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nokia
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
 */
package org.openecomp.sdc.validation.impl.util;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.openecomp.sdc.common.http.client.api.HttpExecuteException;
import org.openecomp.sdc.common.http.client.api.HttpRequestHandler;
import org.openecomp.sdc.common.http.client.api.HttpResponse;
import org.openecomp.sdc.common.http.config.HttpClientConfig;
import org.openecomp.sdc.common.http.config.Timeouts;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.validation.type.helmvalidator.HelmValidatorConfig;

public class HelmValidatorHttpClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(HelmValidatorHttpClient.class);
    private static final int TIMEOUT_MS = 3000;
    private static final String FILE = "file";
    private static final String IS_LINTED = "isLinted";
    private static final String IS_STRICT_LINTED = "isStrictLinted";
    private static final String VERSION_DESIRED = "versionDesired";
    private final HttpRequestHandler httpRequestHandler;

    public HelmValidatorHttpClient(HttpRequestHandler httpRequestHandler) {
        this.httpRequestHandler = httpRequestHandler;
    }

    public HttpResponse<String> execute(String fileName, byte[] helmChartFile, HelmValidatorConfig validatorConfig) throws Exception{
        LOGGER.info("Trying to execute Helm chart validation. File name: {}", fileName);
        try {
            HttpEntity entity = MultipartEntityBuilder.create()
                .addBinaryBody(FILE, helmChartFile, ContentType.DEFAULT_BINARY, fileName)
                .addTextBody(IS_LINTED, getString(validatorConfig.isLintable()))
                .addTextBody(IS_STRICT_LINTED, getString(validatorConfig.isStrictLintable()))
                .addTextBody(VERSION_DESIRED, validatorConfig.getVersion())
                .build();

            HttpResponse<String> httpResponse = httpRequestHandler.post(validatorConfig.getValidatorUrl(),
                null, entity, new HttpClientConfig(new Timeouts(TIMEOUT_MS, TIMEOUT_MS)));
            LOGGER.info("Received response from Helm chart validator with code {}", httpResponse.getStatusCode());
            LOGGER.debug("Response from Helm chart validator: {}", httpResponse);

            return httpResponse;
        } catch (HttpExecuteException e) {
            LOGGER.info("Exception during call to Helm validator {}", e.getMessage());
        }
        throw new Exception("Http response is invalid.");
    }

    private String getString(boolean helmValidatorConfig) {
        return Boolean.toString(helmValidatorConfig);
    }

}
