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

import static javax.ws.rs.core.HttpHeaders.CONTENT_DISPOSITION;
import static org.openecomp.core.utilities.file.FileUtils.getFileExtension;
import static org.openecomp.core.utilities.file.FileUtils.getNetworkPackageName;

import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.inject.Named;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import org.onap.config.api.ConfigurationManager;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.common.errors.ErrorCode;
import org.openecomp.sdc.common.errors.ErrorCodeAndMessage;
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
import org.openecomp.sdcrests.vendorsoftwareproducts.types.UploadFileResponseDto;
import org.openecomp.sdcrests.vsp.rest.VnfPackageRepository;
import org.openecomp.sdcrests.vsp.rest.mapping.MapUploadFileResponseToUploadFileResponseDto;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

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

    private final Configuration config;

    public VnfPackageRepositoryImpl(Configuration config) {
        this.config = config;
    }

    public VnfPackageRepositoryImpl() {
        this(new FileConfiguration());
    }

    @Override
    public Response getVnfPackages(String vspId, String versionId, String user) {

        LOGGER.debug("Get VNF Packages from Repository: {}", vspId);

        Client client = new SharedClient();

        final String getVnfPackageUri = config.getGetUri();

        try {

            Response remoteResponse = client.target(getVnfPackageUri).request().get();
            if (remoteResponse.getStatus() != Response.Status.OK.getStatusCode()) {
                return handleUnexpectedStatus("querying VNF package metadata", getVnfPackageUri, remoteResponse);
            }

            LOGGER.debug("Response from VNF Repository: {}", remoteResponse);
            return Response.ok(remoteResponse.readEntity(String.class)).build();

        } finally {
            client.close();
        }
    }

    @Override
    public Response importVnfPackage(String vspId, String versionId, String csarId, String user) {

        LOGGER.debug("Import VNF Packages from Repository: {}", csarId);

        final String downloadPackageUri = String.format(config.getDownloadUri(), csarId);

        Client client = new SharedClient();

        try {

            Response remoteResponse = client.target(downloadPackageUri).request().get();
            if (remoteResponse.getStatus() != Response.Status.OK.getStatusCode()) {
                return handleUnexpectedStatus("downloading VNF package", downloadPackageUri, remoteResponse);
            }

            LOGGER.debug("Response from VNF Repository for download package is success. URI={}", downloadPackageUri);
            byte[] payload = remoteResponse.readEntity(String.class).getBytes(StandardCharsets.ISO_8859_1);
            return uploadVnfPackage(vspId, versionId, csarId, payload);

        } finally {
            client.close();
        }
    }

    private Response uploadVnfPackage(final String vspId, final String versionId,
                                      final String csarId, final byte[] payload) {
        try {
            final OrchestrationTemplateCandidateManager candidateManager =
                    OrchestrationTemplateCandidateManagerFactory.getInstance().createInterface();
            final String filename = formatFilename(csarId);
            final OnboardPackageInfo onboardPackageInfo = new OnboardPackageInfo(getNetworkPackageName(filename),
                getFileExtension(filename), ByteBuffer.wrap(payload));
            final VspDetails vspDetails = new VspDetails(vspId, getVersion(vspId, versionId));
            final UploadFileResponse response = candidateManager.upload(vspDetails, onboardPackageInfo);
            final UploadFileResponseDto uploadFileResponse =
                new MapUploadFileResponseToUploadFileResponseDto()
                    .applyMapping(response, UploadFileResponseDto.class);

            return Response.ok(uploadFileResponse).build();

        } catch (final Exception e) {
            ErrorCode error = new GeneralErrorBuilder().build();
            LOGGER.error("Exception while uploading package received from VNF Repository", new CoreException(error, e));
            return generateInternalServerError(error);
        }
    }

    @Override
    public Response downloadVnfPackage(String vspId, String versionId, String csarId, String user) {

        LOGGER.debug("Download VNF package from repository: csarId={}", csarId);

        final String downloadPackageUri = String.format(config.getDownloadUri(), csarId);

        Client client = new SharedClient();

        try {

            Response remoteResponse = client.target(downloadPackageUri).request().get();
            if (remoteResponse.getStatus() != Response.Status.OK.getStatusCode()) {
                return handleUnexpectedStatus("downloading VNF package", downloadPackageUri, remoteResponse);
            }

            byte[] payload = remoteResponse.readEntity(String.class).getBytes(StandardCharsets.ISO_8859_1);
            Response.ResponseBuilder response = Response.ok(payload);
            response.header(CONTENT_DISPOSITION, "attachment; filename=" + formatFilename(csarId));

            LOGGER.debug("Response from VNF Repository for download package is success. URI={}", downloadPackageUri);
            return response.build();

        } finally {
            client.close();
        }
    }

    private Version getVersion(String vspId, String versionId) {
        VersioningManager versioningManager = VersioningManagerFactory.getInstance().createInterface();
        return findVersion(versioningManager.list(vspId), versionId).orElse(new Version(versionId));
    }

    Optional<Version> findVersion(List<Version> versions, String requestedVersion) {
        return versions.stream().filter(ver -> Objects.equals(ver.getId(), requestedVersion)).findAny();
    }

    private static Response handleUnexpectedStatus(String action, String uri, Response response) {

        ErrorCode error = new GeneralErrorBuilder().build();

        if (LOGGER.isErrorEnabled()) {
            String body = response.hasEntity() ? response.readEntity(String.class) : "";
            LOGGER.error("Unexpected response status while {}: URI={}, status={}, body={}", action, uri,
                    response.getStatus(), body, new CoreException(error));
        }

        return generateInternalServerError(error);
    }

    private static Response generateInternalServerError(ErrorCode error) {
        ErrorCodeAndMessage payload = new ErrorCodeAndMessage(Response.Status.INTERNAL_SERVER_ERROR, error);
        return Response.serverError().entity(payload).build();
    }

    private static String formatFilename(String csarId) {
        return "temp_" + csarId + ".csar";
    }

    interface Configuration {

        String getGetUri();

        String getDownloadUri();
    }

    private static class SharedClient implements Client {

        private static final Client CLIENT = ClientBuilder.newClient();

        @Override
        public void close() {
            // do not close the shared client
        }

        @Override
        public WebTarget target(String uri) {
            return CLIENT.target(uri);
        }

        @Override
        public WebTarget target(URI uri) {
            return CLIENT.target(uri);
        }

        @Override
        public WebTarget target(UriBuilder uriBuilder) {
            return CLIENT.target(uriBuilder);
        }

        @Override
        public WebTarget target(Link link) {
            return CLIENT.target(link);
        }

        @Override
        public Invocation.Builder invocation(Link link) {
            return CLIENT.invocation(link);
        }

        @Override
        public SSLContext getSslContext() {
            return CLIENT.getSslContext();
        }

        @Override
        public HostnameVerifier getHostnameVerifier() {
            return CLIENT.getHostnameVerifier();
        }

        @Override
        public javax.ws.rs.core.Configuration getConfiguration() {
            return CLIENT.getConfiguration();
        }

        @Override
        public Client property(String name, Object value) {
            return CLIENT.property(name, value);
        }

        @Override
        public Client register(Class<?> componentClass) {
            return CLIENT.register(componentClass);
        }

        @Override
        public Client register(Class<?> componentClass, int priority) {
            return CLIENT.register(componentClass, priority);
        }

        @Override
        public Client register(Class<?> componentClass, Class<?>... contracts) {
            return CLIENT.register(componentClass, contracts);
        }

        @Override
        public Client register(Class<?> componentClass, Map<Class<?>, Integer> contracts) {
            return CLIENT.register(componentClass, contracts);
        }

        @Override
        public Client register(Object component) {
            return CLIENT.register(component);
        }

        @Override
        public Client register(Object component, int priority) {
            return CLIENT.register(component, priority);
        }

        @Override
        public Client register(Object component, Class<?>... contracts) {
            return CLIENT.register(component, contracts);
        }

        @Override
        public Client register(Object component, Map<Class<?>, Integer> contracts) {
            return CLIENT.register(component, contracts);
        }
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
                    LOGGER.error(
                            "Failed to read VNF repository configuration key '{}', default value '{}' will be used",
                            key, defaultValue, e);
                    return defaultValue;
                }
            }

            private static String formatUri(String host, String port, String path) {
                return "http://" + host + ":" + port + (path.startsWith("/") ? path : "/" + path);
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
