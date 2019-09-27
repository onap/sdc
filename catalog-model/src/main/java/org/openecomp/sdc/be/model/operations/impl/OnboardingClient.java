/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.be.model.operations.impl;

import fj.data.Either;
import java.util.Map;
import java.util.Properties;
import org.apache.http.HttpStatus;
import org.openecomp.sdc.be.config.Configuration.OnboardingConfig;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.http.client.api.HttpRequest;
import org.openecomp.sdc.common.http.client.api.HttpResponse;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.common.zip.ZipUtils;

@org.springframework.stereotype.Component("onboarding-client")
public class OnboardingClient {

    private static final Logger log = Logger.getLogger(OnboardingClient.class.getName());

    private static Properties downloadCsarHeaders = new Properties();

    static {
        downloadCsarHeaders.put("Accept", "application/octet-stream");
    }

    public OnboardingClient() {
        super();
    }

    public static void main(String[] args) {

        OnboardingClient csarOperation = new OnboardingClient();

        String csarUuid = "70025CF6081B489CA7B1CBA583D5278D";
        Either<Map<String, byte[]>, StorageOperationStatus> csar = csarOperation.getCsar(csarUuid, null);
        System.out.println(csar.left().value());

    }

    public Either<Map<String, byte[]>, StorageOperationStatus> getCsar(String csarUuid, String userId) {
        String url = buildDownloadCsarUrl() + "/" + csarUuid;

        Properties headers = new Properties();
        if (downloadCsarHeaders != null) {
            downloadCsarHeaders.forEach(headers::put);
        }

        if (userId != null) {
            headers.put(Constants.USER_ID_HEADER, userId);
        }

        log.debug("Url for downloading csar is {}. Headers are {}", url, headers);

        try {
            HttpResponse<byte []> httpResponse = HttpRequest.getAsByteArray(url, headers);
            log.debug("After fetching csar {}. Http return code is {}", csarUuid, httpResponse.getStatusCode());
    
            switch (httpResponse.getStatusCode()) {
            case HttpStatus.SC_OK:
                byte[] data = httpResponse.getResponse();
                if (data != null && data.length > 0) {
                    Map<String, byte[]> readZip = ZipUtils.readZip(data, false);
                    return Either.left(readZip);
                } else {
                    log.debug("Data received from rest is null or empty");
                    return Either.right(StorageOperationStatus.NOT_FOUND);
                }
    
            case HttpStatus.SC_NOT_FOUND:
                return Either.right(StorageOperationStatus.CSAR_NOT_FOUND);
    
            default:
                return Either.right(StorageOperationStatus.GENERAL_ERROR);
            }
        }
        catch(Exception e) {
            log.debug("Request failed with exception", e);
            return Either.right(StorageOperationStatus.GENERAL_ERROR);
        }
    }

    public Either<String, StorageOperationStatus> getPackages(String userId) {
        String url = buildDownloadCsarUrl();

        Properties headers = new Properties();
        headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");

        if (userId != null) {
            headers.put(Constants.USER_ID_HEADER, userId);
        }

        log.debug("Url for downloading packages is {}. Headers are {}", url, headers);

        try {
            HttpResponse<String> httpResposne = HttpRequest.get(url, headers);
            log.debug("After fetching packages. Http return code is {}", httpResposne.getStatusCode());
    
            switch (httpResposne.getStatusCode()) {
            case HttpStatus.SC_OK:
                String data = httpResposne.getResponse();
                return Either.left(data);
    
            case HttpStatus.SC_NOT_FOUND:
                return Either.right(StorageOperationStatus.CSAR_NOT_FOUND);
    
            default:
                return Either.right(StorageOperationStatus.GENERAL_ERROR);
            }
        }
        catch(Exception e) {
            log.debug("Request failed with exception", e);
            return Either.right(StorageOperationStatus.GENERAL_ERROR);
        }
    }

    /**
     * Build the url for download CSAR
     *
     * E.g., http://0.0.0.0:8181/onboarding-api/v1.0/vendor-software-products/packages/
     *
     * @return
     */
    public String buildDownloadCsarUrl() {

        OnboardingConfig onboardingConfig = ConfigurationManager.getConfigurationManager().getConfiguration().getOnboarding();

        String protocol = onboardingConfig.getProtocol();
        String host = onboardingConfig.getHost();
        Integer port = onboardingConfig.getPort();
        String uri = onboardingConfig.getDownloadCsarUri();

        return protocol + "://" + host + ":" + port + uri;
    }

}
