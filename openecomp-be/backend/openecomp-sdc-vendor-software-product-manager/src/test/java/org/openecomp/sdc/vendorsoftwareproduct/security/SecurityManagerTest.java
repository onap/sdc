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

package org.openecomp.sdc.vendorsoftwareproduct.security;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
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
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.FileUtils;
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
import org.openecomp.sdc.be.csar.storage.MinIoArtifactInfo;
import org.openecomp.sdc.common.CommonConfigurationManager;
import org.openecomp.sdc.vendorsoftwareproduct.impl.onboarding.OnboardingPackageProcessor;
import org.openecomp.sdc.vendorsoftwareproduct.impl.onboarding.validation.CnfPackageValidator;
import org.openecomp.sdc.vendorsoftwareproduct.types.OnboardPackageInfo;
import org.openecomp.sdc.vendorsoftwareproduct.types.OnboardSignedPackage;

@ExtendWith(MockitoExtension.class)
class SecurityManagerTest {

    private File certDir;
    private String cerDirPath = "/tmp/cert/";
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

    private File prepareCertFiles(String origFilePath, String newFilePath) throws IOException, URISyntaxException {
        File origFile = new File(getClass().getResource(origFilePath).toURI());
        File newFile = new File(newFilePath);
        newFile.createNewFile();
        FileUtils.copyFile(origFile, newFile);
        return newFile;
    }

    private byte[] readAllBytes(String path) throws URISyntaxException, IOException {
        return Files.readAllBytes(Paths.get(getClass().getResource(path).toURI()));
    }

    @BeforeEach
    public void setUp() throws IOException {
        openMocks(this);
        certDir = new File(cerDirPath);
        if (certDir.exists()) {
            tearDown();
        }
        certDir.mkdirs();
        securityManager = new SecurityManager(certDir.getPath());

    }

    @AfterEach
    public void tearDown() throws IOException {
        if (certDir.exists()) {
            FileUtils.deleteDirectory(certDir);
        }
        securityManager.cleanTrustedCertificates();
    }

    @Test
    void testGetCertificates() throws IOException, SecurityManagerException, URISyntaxException {
        File newFile = prepareCertFiles("/cert/root-certificate.pem", cerDirPath + "/root-certificate.pem");
        assertEquals(1, securityManager.getTrustedCertificates().size());
        newFile.delete();
        assertEquals(0, securityManager.getTrustedCertificates().size());
    }

    @Test
    void testGetCertificatesNoDirectory() throws IOException, SecurityManagerException {
        certDir.delete();
        assertEquals(0, securityManager.getTrustedCertificates().size());
    }

    @Test
    void testGetCertificatesException() throws IOException, SecurityManagerException {
        File newFile = new File(cerDirPath + "root-certificate.pem");
        newFile.createNewFile();
        Assertions.assertThrows(SecurityManagerException.class, () -> {
            assertEquals(1, securityManager.getTrustedCertificates().size());
        });
        newFile.delete();
        assertEquals(0, securityManager.getTrustedCertificates().size());

    }

    @Test
    void testGetCertificatesUpdated() throws IOException, SecurityManagerException, URISyntaxException {
        File newFile = prepareCertFiles("/cert/root-certificate.pem", cerDirPath + "root-certificate.pem");
        assertEquals(1, securityManager.getTrustedCertificates().size());
        File otherNewFile = prepareCertFiles("/cert/package-certificate.pem", cerDirPath + "package-certificate.pem");
        assertEquals(2, securityManager.getTrustedCertificates().size());
        otherNewFile.delete();
        assertEquals(1, securityManager.getTrustedCertificates().size());
        newFile.delete();
        assertEquals(0, securityManager.getTrustedCertificates().size());
    }

    @Test
    void verifySignedDataTestCertIncludedIntoSignature() throws IOException, URISyntaxException, SecurityManagerException {
        prepareCertFiles("/cert/rootCA.cert", cerDirPath + "root.cert");
        byte[] signature = readAllBytes("/cert/2-file-signed-package/dummyPnfv4.cms");
        byte[] archive = readAllBytes("/cert/2-file-signed-package/dummyPnfv4.csar");
        assertTrue(securityManager.verifySignedData(signature, null, archive));
    }

    @Test
    void verifySignedDataTestCertIncludedIntoSignatureArtifactStorageManagerIsEnabled() throws Exception {

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

                prepareCertFiles("/cert/rootCA.cert", cerDirPath + "root.cert");
                byte[] fileToUploadBytes = readAllBytes("/cert/2-file-signed-package/2-file-signed-package.zip");
                when(getObjectArgsBuilder
                    .bucket(anyString())
                    .object(anyString())
                    .build()
                ).thenReturn(getObjectArgs);

                when(minioClient.getObject(any(GetObjectArgs.class)))
                    .thenReturn(new GetObjectResponse(null, "bucket", "", "objectName",
                        new BufferedInputStream(new ByteArrayInputStream(fileToUploadBytes))));

                final var onboardingPackageProcessor = new OnboardingPackageProcessor("2-file-signed-package.zip", fileToUploadBytes,
                    new CnfPackageValidator(), new MinIoArtifactInfo("bucket", "objectName"));
                final OnboardPackageInfo onboardPackageInfo = onboardingPackageProcessor.getOnboardPackageInfo().orElse(null);

                assertTrue(securityManager
                    .verifyPackageSignedData((OnboardSignedPackage) onboardPackageInfo.getOriginalOnboardPackage(),
                        onboardPackageInfo.getArtifactInfo()));
            }
        }
    }

    @Test
    void verifySignedDataTestCertNotIncludedIntoSignatureButExpected() throws IOException, URISyntaxException, SecurityManagerException {
        Assertions.assertThrows(SecurityManagerException.class, () -> {
            prepareCertFiles("/cert/root.cert", cerDirPath + "root.cert");
            byte[] signature = readAllBytes("/cert/3-file-signed-package/dummyPnfv4.cms");
            byte[] archive = readAllBytes("/cert/3-file-signed-package/dummyPnfv4.csar");
            securityManager.verifySignedData(signature, null, archive);
        });

    }

    @Test
    void verifySignedDataTestCertNotIncludedIntoSignature() throws IOException, URISyntaxException, SecurityManagerException {
        prepareCertFiles("/cert/rootCA.cert", cerDirPath + "root.cert");
        byte[] signature = readAllBytes("/cert/3-file-signed-package/dummyPnfv4.cms");
        byte[] archive = readAllBytes("/cert/3-file-signed-package/dummyPnfv4.csar");
        byte[] cert = readAllBytes("/cert/3-file-signed-package/dummyPnfv4.cert");
        assertTrue(securityManager.verifySignedData(signature, cert, archive));
    }

    @Test
    void verifySignedDataTestCertNotIncludedIntoSignatureArtifactStorageManagerIsEnabled() throws Exception {

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
                when(commonConfigurationManager.getConfigValue("externalCsarStore", "tempPath", null)).thenReturn("tempPath");
                when(commonConfigurationManager.getConfigValue(eq("externalCsarStore"), eq("storageType"), any())).thenReturn(MINIO.name());

                prepareCertFiles("/cert/rootCA.cert", cerDirPath + "root.cert");
                byte[] fileToUploadBytes = readAllBytes("/cert/3-file-signed-package/3-file-signed-package.zip");
                when(getObjectArgsBuilder
                    .bucket(anyString())
                    .object(anyString())
                    .build()
                ).thenReturn(getObjectArgs);

                when(minioClient.getObject(any(GetObjectArgs.class)))
                    .thenReturn(new GetObjectResponse(null, "bucket", "", "objectName",
                        new BufferedInputStream(new ByteArrayInputStream(fileToUploadBytes))));

                final var onboardingPackageProcessor = new OnboardingPackageProcessor("3-file-signed-package.zip", fileToUploadBytes,
                    new CnfPackageValidator(), new MinIoArtifactInfo("bucket", "objectName"));
                final OnboardPackageInfo onboardPackageInfo = onboardingPackageProcessor.getOnboardPackageInfo().orElse(null);

                assertTrue(securityManager
                    .verifyPackageSignedData((OnboardSignedPackage) onboardPackageInfo.getOriginalOnboardPackage(),
                        onboardPackageInfo.getArtifactInfo()));
            }
        }
    }

    @Test
    void verifySignedDataTestCertIntermediateNotIncludedIntoSignature() throws IOException, URISyntaxException, SecurityManagerException {
        prepareCertFiles("/cert/rootCA.cert", cerDirPath + "root.cert");
        prepareCertFiles("/cert/package2.cert", cerDirPath + "signing-ca2.crt");
        byte[] signature = readAllBytes("/cert/3-file-signed-package/dummyPnfv4.cms");
        byte[] archive = readAllBytes("/cert/3-file-signed-package/dummyPnfv4.csar");
        byte[] cert = readAllBytes("/cert/3-file-signed-package/dummyPnfv4.cert");
        assertTrue(securityManager.verifySignedData(signature, cert, archive));
    }

    @Test
    void verifySignedDataTestCertWrongIntermediate() throws IOException, URISyntaxException, SecurityManagerException {
        Assertions.assertThrows(SecurityManagerException.class, () -> {
            prepareCertFiles("/cert/root.cert", cerDirPath + "root.cert");
            prepareCertFiles("/cert/signing-ca1.crt", cerDirPath + "signing-ca1.crt");
            byte[] signature = readAllBytes("/cert/3-file-signed-package/dummyPnfv4.cms");
            byte[] archive = readAllBytes("/cert/3-file-signed-package/dummyPnfv4.csar");
            byte[] cert = readAllBytes("/cert/3-file-signed-package/dummyPnfv4-no-intermediate.cert");
            securityManager.verifySignedData(signature, cert, archive);
        });

    }

    @Test
    void verifySignedDataTestCertIncludedIntoSignatureWithWrongIntermediateInDirectory()
        throws IOException, URISyntaxException, SecurityManagerException {
        prepareCertFiles("/cert/rootCA.cert", cerDirPath + "root.cert");
        prepareCertFiles("/cert/signing-ca1.crt", cerDirPath + "signing-ca1.crt");
        byte[] signature = readAllBytes("/cert/2-file-signed-package/dummyPnfv4.cms");
        byte[] archive = readAllBytes("/cert/2-file-signed-package/dummyPnfv4.csar");
        assertTrue(securityManager.verifySignedData(signature, null, archive));
    }

    @Test
    void verifySignedDataTestCertWrongIntermediateInDirectory() throws IOException, URISyntaxException, SecurityManagerException {
        prepareCertFiles("/cert/rootCA.cert", cerDirPath + "root.cert");
        prepareCertFiles("/cert/signing-ca1.crt", cerDirPath + "signing-ca1.crt");
        byte[] signature = readAllBytes("/cert/3-file-signed-package/dummyPnfv4.cms");
        byte[] archive = readAllBytes("/cert/3-file-signed-package/dummyPnfv4.csar");
        byte[] cert = readAllBytes("/cert/3-file-signed-package/dummyPnfv4.cert");
        assertTrue(securityManager.verifySignedData(signature, cert, archive));
    }

    @Test
    void verifySignedDataTestWrongCertificate() throws IOException, URISyntaxException, SecurityManagerException {
        Assertions.assertThrows(SecurityManagerException.class, () -> {
            prepareCertFiles("/cert/root-certificate.pem", cerDirPath + "root-certificate.cert");
            byte[] signature = readAllBytes("/cert/3-file-signed-package/dummyPnfv4.cms");
            byte[] archive = readAllBytes("/cert/3-file-signed-package/dummyPnfv4.csar");
            byte[] cert = readAllBytes("/cert/3-file-signed-package/dummyPnfv4.cert");
            securityManager.verifySignedData(signature, cert, archive);
        });

    }

    @Test
    void verifySignedDataTestChangedArchive() throws IOException, URISyntaxException, SecurityManagerException {
        Assertions.assertThrows(SecurityManagerException.class, () -> {
            prepareCertFiles("/cert/root.cert", cerDirPath + "root.cert");
            byte[] signature = readAllBytes("/cert/tampered-signed-package/dummyPnfv4.cms");
            byte[] archive = readAllBytes("/cert/tampered-signed-package/dummyPnfv4.csar");
            securityManager.verifySignedData(signature, null, archive);
        });

    }
}
