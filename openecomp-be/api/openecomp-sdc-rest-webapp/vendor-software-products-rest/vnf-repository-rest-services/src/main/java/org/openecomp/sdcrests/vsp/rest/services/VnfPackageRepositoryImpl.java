/*
 * Copyright 2017 Huawei Technologies Co., Ltd.
 * Modifications Copyright 2018 European Support Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openecomp.sdcrests.vsp.rest.services;

import static org.openecomp.core.utilities.file.FileUtils.getFileExtension;
import static org.openecomp.core.utilities.file.FileUtils.getNetworkPackageName;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.inject.Named;
import javax.net.ssl.SSLContext;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.onap.config.api.ConfigurationManager;
import org.onap.config.api.JettySSLUtils;
import org.openecomp.core.utilities.orchestration.OnboardingTypesEnum;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.common.errors.ErrorCode;
import org.openecomp.sdc.common.errors.GeneralErrorBuilder;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.OrchestrationTemplateCandidateManager;
import org.openecomp.sdc.vendorsoftwareproduct.OrchestrationTemplateCandidateManagerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.vendorsoftwareproduct.types.OnboardPackageInfo;
import org.openecomp.sdc.vendorsoftwareproduct.types.UploadFileResponse;
import org.openecomp.sdc.versioning.VersioningManager;
import org.openecomp.sdc.versioning.VersioningManagerFactory;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdcrests.errors.ErrorCodeAndMessage;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.UploadFileResponseDto;
import org.openecomp.sdcrests.vsp.rest.VnfPackageRepository;
import org.openecomp.sdcrests.vsp.rest.mapping.MapUploadFileResponseToUploadFileResponseDto;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * Enables integration API interface with VNF Repository (VNFSDK).
 * <ol>
 *     <li>Get all the VNF Package Meta-data.</li>
 *     <li>Download a VNF Package.</li>
 *     <li>Import a VNF package to SDC catalog (Download & validate).</li>
 * </ol>
 *
 * @version Amsterdam release (ONAP 1.0)
 */
@Named
@Service("vnfPackageRepository")
@Scope(value = "prototype")
public class VnfPackageRepositoryImpl implements VnfPackageRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(VnfPackageRepositoryImpl.class);
    private RestTemplate restTemplate = trustSSLClient();

    public RestTemplate trustSSLClient() throws Exception {

        SSLContext sslcontext = JettySSLUtils.getSslContext();

        HttpClient httpClient = HttpClients.custom()
                .setSSLContext(sslcontext) // Use the SSLContext obtained
                .setSSLHostnameVerifier((requestedHost, remoteServerSession) ->
                        requestedHost.equalsIgnoreCase(remoteServerSession.getPeerHost())) // Custom hostname verifier
                .build();

        return new RestTemplate(new HttpComponentsClientHttpRequestFactory(httpClient));
    }


    private final Configuration config;

    public VnfPackageRepositoryImpl(Configuration config) throws Exception {
        this.config = config;
    }

    public VnfPackageRepositoryImpl() throws Exception {
        this(new FileConfiguration());
        this.restTemplate = trustSSLClient();
    }

    @Override
    public ResponseEntity getVnfPackages(String vspId, String versionId, String user) {
        LOGGER.debug("Get VNF Packages from Repository: {}", vspId);
        final String getVnfPackageUri = config.getGetUri();
        ResponseEntity<String> remoteResponse;
        try {
            remoteResponse = restTemplate.getForEntity(getVnfPackageUri, String.class);
            if (remoteResponse.getStatusCode() != HttpStatus.OK) {
                return handleUnexpectedStatus("querying VNF package metadata", getVnfPackageUri, remoteResponse);
            }
        } catch (HttpClientErrorException.Forbidden e) {
            LOGGER.error("Forbidden error while downloading VNF package: URI={}", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        LOGGER.debug("Response from VNF Repository: {}", remoteResponse);
        return new ResponseEntity<>(remoteResponse.getBody(), HttpStatus.OK);
    }

    @Override
    public ResponseEntity importVnfPackage(String vspId, String versionId, String csarId, String user) {
        LOGGER.debug("Import VNF Packages from Repository: {}", csarId);
        final String downloadPackageUri = String.format(config.getDownloadUri(), csarId);
        ResponseEntity<String> remoteResponse = restTemplate.getForEntity(downloadPackageUri, String.class);
        if (remoteResponse.getStatusCode() != HttpStatus.OK) {
            return handleUnexpectedStatus("downloading VNF package", downloadPackageUri, remoteResponse);
        }
        LOGGER.debug("Response from VNF Repository for download package is success. URI={}", downloadPackageUri);
        byte[] payload = remoteResponse.getBody().getBytes(StandardCharsets.ISO_8859_1);
        return uploadVnfPackage(vspId, versionId, csarId, payload);
    }

    private ResponseEntity uploadVnfPackage(final String vspId, final String versionId, final String csarId, final byte[] payload) {
        try {
            final OrchestrationTemplateCandidateManager candidateManager = OrchestrationTemplateCandidateManagerFactory.getInstance()
                .createInterface();
            final String filename = formatFilename(csarId);
            final String fileExtension = getFileExtension(filename);
            final OnboardPackageInfo onboardPackageInfo = new OnboardPackageInfo(getNetworkPackageName(filename), fileExtension,
                ByteBuffer.wrap(payload), OnboardingTypesEnum.getOnboardingTypesEnum(fileExtension));
            final VspDetails vspDetails = new VspDetails(vspId, getVersion(vspId, versionId));
            final UploadFileResponse response = candidateManager.upload(vspDetails, onboardPackageInfo);
            final UploadFileResponseDto uploadFileResponse = new MapUploadFileResponseToUploadFileResponseDto()
                .applyMapping(response, UploadFileResponseDto.class);
            return ResponseEntity.ok(uploadFileResponse);
        } catch (final Exception e) {
            ErrorCode error = new GeneralErrorBuilder().build();
            LOGGER.error("Exception while uploading package received from VNF Repository", new CoreException(error, e));
            return generateInternalServerError(error);
        }
    }

    @Override
    public ResponseEntity downloadVnfPackage(String vspId, String versionId, String csarId, String user) {
        LOGGER.debug("Download VNF package from repository: csarId={}", csarId);
        final String downloadPackageUri = String.format(config.getDownloadUri(), csarId);
        ResponseEntity<String> remoteResponse = null;
        try {
            remoteResponse = restTemplate.getForEntity(downloadPackageUri, String.class);
            if (remoteResponse.getStatusCode() != HttpStatus.OK) {
                return handleUnexpectedStatus("downloading VNF package", downloadPackageUri, remoteResponse);
            }
        } catch (HttpClientErrorException.Forbidden e) {
            LOGGER.error("Forbidden error while downloading VNF package: URI={}", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        byte[] payload = remoteResponse.getBody().getBytes(StandardCharsets.ISO_8859_1);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + formatFilename(csarId));
        LOGGER.debug("Response from VNF Repository for download package is success. URI={}", downloadPackageUri);
        return new ResponseEntity<>(payload, headers, HttpStatus.OK);
    }

    private Version getVersion(String vspId, String versionId) {
        VersioningManager versioningManager = VersioningManagerFactory.getInstance().createInterface();
        return findVersion(versioningManager.list(vspId), versionId).orElse(new Version(versionId));
    }

    Optional<Version> findVersion(List<Version> versions, String requestedVersion) {
        return versions.stream().filter(ver -> Objects.equals(ver.getId(), requestedVersion)).findAny();
    }

    private static ResponseEntity handleUnexpectedStatus(String action, String uri, ResponseEntity response) {
        ErrorCode error = new GeneralErrorBuilder().build();
        if (LOGGER.isErrorEnabled()) {
            String body = (response.getBody() != null) ? (String) response.getBody() : "";
            LOGGER.error("Unexpected response status while {}: URI={}, status={}, body={}", action, uri, response.getStatusCodeValue(), body,
                new CoreException(error));
        }
        return generateInternalServerError(error);
    }

    private static ResponseEntity<ErrorCodeAndMessage> generateInternalServerError(ErrorCode error) {
        ErrorCodeAndMessage payload = new ErrorCodeAndMessage(HttpStatus.INTERNAL_SERVER_ERROR, error);
        return new ResponseEntity<>(payload, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private static String formatFilename(String csarId) {
        return "temp_" + csarId + ".csar";
    }

    interface Configuration {

        String getGetUri();

        String getDownloadUri();
    }

    static class FileConfiguration implements Configuration {

        @Override
        public String getGetUri() {
            return LazyFileConfiguration.INSTANCE.getGetUri();
        }

        @Override
        public String getDownloadUri() {
            return LazyFileConfiguration.INSTANCE.getDownloadUri();
        }

        private static class LazyFileConfiguration implements Configuration {

            private static final String CONFIG_NAMESPACE = "vnfrepo";
            private static final String DEFAULT_HOST = "localhost";
            private static final String DEFAULT_PORT = "8702";
            private static final String DEFAULT_URI_PREFIX = "/onapapi/vnfsdk-marketplace/v1/PackageResource/csars";
            private static final String DEFAULT_LIST_URI = DEFAULT_URI_PREFIX + "/";
            private static final String DEFAULT_DOWNLOAD_URI = DEFAULT_URI_PREFIX + "/%s/files";
            private static final LazyFileConfiguration INSTANCE = new LazyFileConfiguration();
            private final String getUri;
            private final String downloadUri;

            private LazyFileConfiguration() {
                org.onap.config.api.Configuration config = ConfigurationManager.lookup();
                String host = readConfig(config, "vnfRepoHost", DEFAULT_HOST);
                String port = readConfig(config, "vnfRepoPort", DEFAULT_PORT);
                String listPackagesUri = readConfig(config, "getVnfUri", DEFAULT_LIST_URI);
                String downloadPackageUri = readConfig(config, "downloadVnfUri", DEFAULT_DOWNLOAD_URI);
                this.getUri = formatUri(host, port, listPackagesUri);
                this.downloadUri = formatUri(host, port, downloadPackageUri);
            }

            private String readConfig(org.onap.config.api.Configuration config, String key, String defaultValue) {
                try {
                    String value = config.getAsString(CONFIG_NAMESPACE, key);
                    return (value == null) ? defaultValue : value;
                } catch (Exception e) {
                    LOGGER.error("Failed to read VNF repository configuration key '{}', default value '{}' will be used", key, defaultValue, e);
                    return defaultValue;
                }
            }

            private static String formatUri(String host, String port, String path) {
                return "https://" + host + ":" + port + (path.startsWith("/") ? path : "/" + path);
            }

            public String getGetUri() {
                return getUri;
            }

            public String getDownloadUri() {
                return downloadUri;
            }
        }
    }
}
