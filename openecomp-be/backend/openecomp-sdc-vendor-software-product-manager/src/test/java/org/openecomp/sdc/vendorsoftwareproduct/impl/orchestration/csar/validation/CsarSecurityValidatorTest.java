/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation
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

package org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.csar.validation;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;
import static org.openecomp.sdc.be.csar.storage.StorageFactory.StorageType.MINIO;

import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.MinioClient;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openecomp.sdc.be.csar.storage.ArtifactInfo;
import org.openecomp.sdc.be.csar.storage.MinIoArtifactInfo;
import org.openecomp.sdc.common.CommonConfigurationManager;
import org.openecomp.sdc.vendorsoftwareproduct.impl.onboarding.OnboardingPackageProcessor;
import org.openecomp.sdc.vendorsoftwareproduct.impl.onboarding.validation.CnfPackageValidator;
import org.openecomp.sdc.vendorsoftwareproduct.security.SecurityManager;
import org.openecomp.sdc.vendorsoftwareproduct.security.SecurityManagerException;
import org.openecomp.sdc.vendorsoftwareproduct.types.OnboardPackageInfo;
import org.openecomp.sdc.vendorsoftwareproduct.types.OnboardSignedPackage;

@ExtendWith(MockitoExtension.class)
class CsarSecurityValidatorTest {

    private static final String BASE_DIR = "/vspmanager.csar/signing/";
    private static final String DELIMITER = "---";
    private CsarSecurityValidator csarSecurityValidator;
    @Mock
    private SecurityManager securityManager;
    @Mock
    private CommonConfigurationManager commonConfigurationManager;
    @Mock
    private MinioClient minioClient;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private MinioClient.Builder builderMinio;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private GetObjectArgs.Builder getObjectArgsBuilder;
    @Mock
    private GetObjectArgs getObjectArgs;

    @AfterEach
    void tearDown() throws Exception {
        restore();
    }

    private void restore() throws Exception {
        final URI uri = CsarSecurityValidatorTest.class.getResource(BASE_DIR).toURI();
        final List<Path> list = Files.list(Path.of(uri.getPath())).filter(path -> path.toString().contains(DELIMITER)).collect(Collectors.toList());
        for (final Path path : list) {
            final String[] split = path.toString().split(DELIMITER);
            Files.move(path, Path.of(split[0]), REPLACE_EXISTING);
        }
    }

    @BeforeEach
    public void setUp() throws Exception {
        openMocks(this);
        csarSecurityValidator = new CsarSecurityValidator(securityManager);
        backup();
    }

    private void backup() throws Exception {
        final URI uri = CsarSecurityValidatorTest.class.getResource(BASE_DIR).toURI();
        final List<Path> list = Files.list(Path.of(uri.getPath())).collect(Collectors.toList());
        for (final Path path : list) {
            Files.copy(path, Path.of(path.toString() + DELIMITER + UUID.randomUUID()), REPLACE_EXISTING);
        }
    }

    @Test
    void isSignatureValidTestCorrectStructureAndValidSignatureExists() throws SecurityManagerException {
        final byte[] packageBytes = getFileBytesOrFail("signed-package.zip");
        final OnboardPackageInfo onboardPackageInfo = loadSignedPackageWithArtifactInfoS3Store("signed-package.zip", packageBytes, null);
        when(securityManager.verifyPackageSignedData(any(OnboardSignedPackage.class), any(ArtifactInfo.class))).thenReturn(true);
        final boolean isSignatureValid = csarSecurityValidator
            .verifyPackageSignature((OnboardSignedPackage) onboardPackageInfo.getOriginalOnboardPackage(), onboardPackageInfo.getArtifactInfo());
        assertThat("Signature should be valid", isSignatureValid, is(true));
    }

    @Test
    void isSignatureValidTestCorrectStructureAndNotValidSignatureExists() throws Exception {

        final Map<String, Object> endpoint = new HashMap<>();
        endpoint.put("host", "localhost");
        endpoint.put("port", 9000);
        final Map<String, Object> credentials = new HashMap<>();
        credentials.put("accessKey", "login");
        credentials.put("secretKey", "password");

        try (MockedStatic<CommonConfigurationManager> utilities = Mockito.mockStatic(CommonConfigurationManager.class)) {
            utilities.when(CommonConfigurationManager::getInstance).thenReturn(commonConfigurationManager);
            try (MockedStatic<MinioClient> minioUtilities = Mockito.mockStatic(MinioClient.class)) {
                minioUtilities.when(MinioClient::builder).thenReturn(builderMinio);
                when(builderMinio
                    .endpoint(anyString(), anyInt(), anyBoolean())
                    .credentials(anyString(), anyString())
                    .build()
                ).thenReturn(minioClient);

                when(commonConfigurationManager.getConfigValue("externalCsarStore", "endpoint", null)).thenReturn(endpoint);
                when(commonConfigurationManager.getConfigValue("externalCsarStore", "credentials", null)).thenReturn(credentials);
                when(commonConfigurationManager.getConfigValue("externalCsarStore", "tempPath", null)).thenReturn("cert/2-file-signed-package");
                when(commonConfigurationManager.getConfigValue(eq("externalCsarStore"), eq("storageType"), any())).thenReturn(MINIO.name());

                final byte[] packageBytes = getFileBytesOrFail("signed-package-tampered-data.zip");

                when(getObjectArgsBuilder
                    .bucket(anyString())
                    .object(anyString())
                    .build()
                ).thenReturn(getObjectArgs);

                when(minioClient.getObject(any(GetObjectArgs.class)))
                    .thenReturn(new GetObjectResponse(null, "bucket", "", "objectName",
                        new BufferedInputStream(new ByteArrayInputStream(packageBytes))));

                final OnboardPackageInfo onboardPackageInfo = loadSignedPackageWithArtifactInfoS3Store("signed-package-tampered-data.zip",
                    packageBytes,
                    null);
                //no mocked securityManager
                csarSecurityValidator = new CsarSecurityValidator();
                Assertions.assertThrows(SecurityManagerException.class, () -> {
                    csarSecurityValidator.verifyPackageSignature((OnboardSignedPackage) onboardPackageInfo.getOriginalOnboardPackage(),
                        onboardPackageInfo.getArtifactInfo());
                });
            }
        }
    }

    @Test
    void isSignatureValidTestCorrectStructureAndValidSignatureExistsArtifactStorageManagerIsEnabled() throws SecurityManagerException {
        final byte[] packageBytes = getFileBytesOrFail("signed-package.zip");
        final OnboardPackageInfo onboardPackageInfo = loadSignedPackageWithoutArtifactInfo("signed-package.zip",
            packageBytes, null);
        when(securityManager.verifySignedData(any(), any(), any())).thenReturn(true);
        final boolean isSignatureValid = csarSecurityValidator
            .verifyPackageSignature((OnboardSignedPackage) onboardPackageInfo.getOriginalOnboardPackage(), onboardPackageInfo.getArtifactInfo());

        assertThat("Signature should be valid", isSignatureValid, is(true));
    }

    @Test
    void isSignatureValidTestCorrectStructureAndNotValidSignatureExistsArtifactStorageManagerIsEnabled() throws SecurityManagerException {
        final byte[] packageBytes = getFileBytesOrFail("signed-package-tampered-data.zip");
        final OnboardPackageInfo onboardPackageInfo = loadSignedPackageWithoutArtifactInfo("signed-package-tampered-data.zip",
            packageBytes, null);
        //no mocked securityManager
        csarSecurityValidator = new CsarSecurityValidator();
        Assertions.assertThrows(SecurityManagerException.class, () -> {
            csarSecurityValidator
                .verifyPackageSignature((OnboardSignedPackage) onboardPackageInfo.getOriginalOnboardPackage(), onboardPackageInfo.getArtifactInfo());
        });
    }

    private byte[] getFileBytesOrFail(final String path) {
        try {
            return getFileBytes(path);
        } catch (final URISyntaxException | IOException e) {
            fail("Could not load file " + path);
            return null;
        }
    }

    private byte[] getFileBytes(final String path) throws URISyntaxException, IOException {
        return Files.readAllBytes(Paths.get(
            CsarSecurityValidatorTest.class.getResource(BASE_DIR + path).toURI()));
    }

    private OnboardPackageInfo loadSignedPackageWithArtifactInfoS3Store(final String packageName, final byte[] packageBytes,
                                                                        final CnfPackageValidator cnfPackageValidator) {
        final OnboardingPackageProcessor onboardingPackageProcessor =
            new OnboardingPackageProcessor(packageName, packageBytes, cnfPackageValidator, new MinIoArtifactInfo("bucket", "object"));
        final OnboardPackageInfo onboardPackageInfo = onboardingPackageProcessor.getOnboardPackageInfo().orElse(null);
        if (onboardPackageInfo == null) {
            fail("Unexpected error. Could not load original package");
        }

        return onboardPackageInfo;
    }

    private OnboardPackageInfo loadSignedPackageWithoutArtifactInfo(final String packageName, final byte[] packageBytes,
                                                                    final CnfPackageValidator cnfPackageValidator) {
        final OnboardingPackageProcessor onboardingPackageProcessor =
            new OnboardingPackageProcessor(packageName, packageBytes, cnfPackageValidator, null);
        final OnboardPackageInfo onboardPackageInfo = onboardingPackageProcessor.getOnboardPackageInfo().orElse(null);
        if (onboardPackageInfo == null) {
            fail("Unexpected error. Could not load original package");
        }

        return onboardPackageInfo;
    }
}
