/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Modification Copyright (C) 2021 Nordix Foundation.
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

package org.openecomp.sdc.be.client.onboarding.impl;

import static javax.ws.rs.core.HttpHeaders.ACCEPT;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fj.data.Either;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import javax.ws.rs.core.MediaType;
import org.apache.http.HttpStatus;
import org.openecomp.sdc.be.client.onboarding.api.OnboardingClient;
import org.openecomp.sdc.be.client.onboarding.exception.OnboardingClientException;
import org.openecomp.sdc.be.config.Configuration.OnboardingConfig;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.model.VendorSoftwareProduct;
import org.openecomp.sdc.be.model.dto.VendorSoftwareProductDto;
import org.openecomp.sdc.be.model.mapper.VendorSoftwareProductMapper;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.http.client.api.HttpRequest;
import org.openecomp.sdc.common.http.client.api.HttpResponse;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.common.zip.ZipUtils;

@org.springframework.stereotype.Component("onboarding-client")
public class OnboardingClientImpl implements OnboardingClient {

    private static final Logger LOGGER = Logger.getLogger(OnboardingClientImpl.class);
    private final Properties downloadCsarHeaders;

    public OnboardingClientImpl() {
        downloadCsarHeaders = new Properties();
        downloadCsarHeaders.put(ACCEPT, MediaType.APPLICATION_OCTET_STREAM);
    }

    @Override
    public Either<Map<String, byte[]>, StorageOperationStatus> findLatestPackage(final String vspId, final String userId) {
        final String url = buildGetLatestPackageUrl(vspId);
        return handleGetPackage(userId, url);
    }

    @Override
    public Either<Map<String, byte[]>, StorageOperationStatus> findPackage(final String vspId, final String versionId, final String userId) {
        final String url = buildGetCsarUrl(vspId, versionId);
        return handleGetPackage(userId, url);
    }

    private Either<Map<String, byte[]>, StorageOperationStatus> handleGetPackage(final String userId, final String url) {
        final Properties headers = buildDefaultHeader(userId);
        downloadCsarHeaders.forEach(headers::put);
        LOGGER.debug("Get VSP package URL is '{}'. Used headers '{}'", url, headers);
        try {
            final HttpResponse<byte[]> httpResponse = HttpRequest.getAsByteArray(url, headers);
            LOGGER.debug("'{}' HTTP response status was '{}'", url, httpResponse.getStatusCode());
            switch (httpResponse.getStatusCode()) {
                case HttpStatus.SC_OK:
                    byte[] data = httpResponse.getResponse();
                    if (data != null && data.length > 0) {
                        Map<String, byte[]> readZip = ZipUtils.readZip(data, false);
                        return Either.left(readZip);
                    }
                    LOGGER.debug("Empty payload received from '{}'", url);
                    return Either.right(StorageOperationStatus.NOT_FOUND);
                case HttpStatus.SC_NOT_FOUND:
                    return Either.right(StorageOperationStatus.CSAR_NOT_FOUND);
                default:
                    return Either.right(StorageOperationStatus.GENERAL_ERROR);
            }
        } catch (final Exception e) {
            LOGGER.debug("Get VSP package request failed with exception", e);
            return Either.right(StorageOperationStatus.GENERAL_ERROR);
        }
    }

    @Override
    public Optional<VendorSoftwareProduct> findVendorSoftwareProduct(final String id, final String versionId, final String userId) {
        final Either<Map<String, byte[]>, StorageOperationStatus> csarEither = this.findPackage(id, versionId, userId);
        if (csarEither.isRight()) {
            final StorageOperationStatus operationStatus = csarEither.right().value();
            if (operationStatus == StorageOperationStatus.CSAR_NOT_FOUND || operationStatus == StorageOperationStatus.NOT_FOUND) {
                return Optional.empty();
            }
            var errorMsg = String.format("An error has occurred while retrieving the package with id '%s' and versionId '%s': '%s'",
                id, versionId, operationStatus);
            throw new OnboardingClientException(errorMsg);
        }
        final String url = buildGetVspUrl(id, versionId);
        final Properties headers = buildDefaultHeader(userId);
        headers.put(ACCEPT, APPLICATION_JSON);
        LOGGER.debug("Find VSP built url '{}', with headers '{}'", url, headers);
        final HttpResponse<String> httpResponse;
        try {
            httpResponse = HttpRequest.get(url, headers);
        } catch (final Exception e) {
            throw new OnboardingClientException("An error has occurred while retrieving the package", e);
        }

        if (httpResponse.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
            return Optional.empty();
        }

        if (httpResponse.getStatusCode() != HttpStatus.SC_OK) {
            var errorMsg = String.format("An error has occurred while retrieving the package. Http status was %s", httpResponse.getStatusCode());
            throw new OnboardingClientException(errorMsg);
        }

        final String responseData = httpResponse.getResponse();
        LOGGER.debug("Find vsp response data: '{}'", responseData);

        final VendorSoftwareProductDto vendorSoftwareProductDto;
        try {
            vendorSoftwareProductDto = new ObjectMapper().readValue(responseData, VendorSoftwareProductDto.class);
        } catch (final JsonProcessingException e) {
            throw new OnboardingClientException("Could not parse retrieve package response to VendorSoftwareProductDto.class.", e);
        }
        final Map<String, byte[]> csarFileMap = csarEither.left().value();
        final var vendorSoftwareProduct = VendorSoftwareProductMapper.mapFrom(vendorSoftwareProductDto);
        vendorSoftwareProduct.setFileMap(csarFileMap);
        return Optional.of(vendorSoftwareProduct);
    }

    private Properties buildDefaultHeader(final String userId) {
        final var headers = new Properties();
        if (userId != null) {
            headers.put(Constants.USER_ID_HEADER, userId);
        }
        return headers;
    }

    private String buildGetCsarUrl(final String vspId, final String versionId) {
        final var onboardingConfig = getOnboardingConfig();
        final var uri = String.format(onboardingConfig.getGetVspPackageUri(), vspId, versionId);
        return buildBaseOnboardingUrl() + uri;
    }

    private String buildGetLatestPackageUrl(final String vspId) {
        final var onboardingConfig = getOnboardingConfig();
        final var uri = String.format(onboardingConfig.getGetLatestVspPackageUri(), vspId);
        return buildBaseOnboardingUrl() + uri;
    }

    private String buildBaseOnboardingUrl() {
        final var onboardingConfig = getOnboardingConfig();
        final String protocol = onboardingConfig.getProtocol();
        final String host = onboardingConfig.getHost();
        final Integer port = onboardingConfig.getPort();
        return String.format("%s://%s:%s", protocol, host, port);
    }

    private String buildGetVspUrl(final String id, final String versionId) {
        final var onboardingConfig = getOnboardingConfig();
        final String protocol = onboardingConfig.getProtocol();
        final String host = onboardingConfig.getHost();
        final Integer port = onboardingConfig.getPort();
        final var uri = String.format(onboardingConfig.getGetVspUri(), id, versionId);
        return String.format("%s://%s:%s%s", protocol, host, port, uri);
    }

    private OnboardingConfig getOnboardingConfig() {
        return ConfigurationManager.getConfigurationManager().getConfiguration().getOnboarding();
    }

}
