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

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SecurityManagerTest {
    private File certDir;
    private String cerDirPath = "/tmp/cert/";
    private SecurityManager securityManager;

    private File PrepareCertFiles(String origFilePath, String newFilePath) throws IOException, URISyntaxException {
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
    public void testGetCertificates() throws IOException, SecurityManagerException, URISyntaxException {
        File newFile = PrepareCertFiles("/cert/root-certificate.pem", cerDirPath + "/root-certificate.pem");
        assertEquals(1, securityManager.getTrustedCertificates().size());
        newFile.delete();
        assertEquals(0, securityManager.getTrustedCertificates().size());
    }

    @Test
    public void testGetCertificatesNoDirectory() throws IOException, SecurityManagerException {
        certDir.delete();
        assertEquals(0, securityManager.getTrustedCertificates().size());
    }

    @Test
    public void testGetCertificatesException() throws IOException, SecurityManagerException {
        File newFile = new File(cerDirPath + "root-certificate.pem");
        newFile.createNewFile();
        Assertions.assertThrows(SecurityManagerException.class, () -> {
            assertEquals(1, securityManager.getTrustedCertificates().size());
        });
        newFile.delete();
        assertEquals(0, securityManager.getTrustedCertificates().size());

    }

    @Test
    public void testGetCertificatesUpdated() throws IOException, SecurityManagerException, URISyntaxException {
        File newFile = PrepareCertFiles("/cert/root-certificate.pem", cerDirPath + "root-certificate.pem");
        assertTrue(securityManager.getTrustedCertificates().size() == 1);
        File otherNewFile = PrepareCertFiles("/cert/package-certificate.pem", cerDirPath + "package-certificate.pem");
        assertEquals(2, securityManager.getTrustedCertificates().size());
        otherNewFile.delete();
        assertEquals(1, securityManager.getTrustedCertificates().size());
        newFile.delete();
        assertEquals(0, securityManager.getTrustedCertificates().size());
    }

    @Test
    public void verifySignedDataTestCertIncludedIntoSignature() throws IOException, URISyntaxException, SecurityManagerException {
        PrepareCertFiles("/cert/rootCA.cert", cerDirPath + "root.cert");
        byte[] signature = readAllBytes("/cert/2-file-signed-package/dummyPnfv4.cms");
        byte[] archive = readAllBytes("/cert/2-file-signed-package/dummyPnfv4.csar");
        assertTrue(securityManager.verifySignedData(signature, null, archive));
    }

    @Test
    public void verifySignedDataTestCertNotIncludedIntoSignatureButExpected() throws IOException, URISyntaxException, SecurityManagerException {
        Assertions.assertThrows(SecurityManagerException.class, () -> {
            PrepareCertFiles("/cert/root.cert", cerDirPath + "root.cert");
            byte[] signature = readAllBytes("/cert/3-file-signed-package/dummyPnfv4.cms");
            byte[] archive = readAllBytes("/cert/3-file-signed-package/dummyPnfv4.csar");
            securityManager.verifySignedData(signature, null, archive);
        });

    }

    @Test
    public void verifySignedDataTestCertNotIncludedIntoSignature() throws IOException, URISyntaxException, SecurityManagerException {
        PrepareCertFiles("/cert/rootCA.cert", cerDirPath + "root.cert");
        byte[] signature = readAllBytes("/cert/3-file-signed-package/dummyPnfv4.cms");
        byte[] archive = readAllBytes("/cert/3-file-signed-package/dummyPnfv4.csar");
        byte[] cert = readAllBytes("/cert/3-file-signed-package/dummyPnfv4.cert");
        assertTrue(securityManager.verifySignedData(signature, cert, archive));
    }

    @Test
    public void verifySignedDataTestCertIntermediateNotIncludedIntoSignature() throws IOException, URISyntaxException, SecurityManagerException {
        PrepareCertFiles("/cert/rootCA.cert", cerDirPath + "root.cert");
        PrepareCertFiles("/cert/package2.cert", cerDirPath + "signing-ca2.crt");
        byte[] signature = readAllBytes("/cert/3-file-signed-package/dummyPnfv4.cms");
        byte[] archive = readAllBytes("/cert/3-file-signed-package/dummyPnfv4.csar");
        byte[] cert = readAllBytes("/cert/3-file-signed-package/dummyPnfv4.cert");
        assertTrue(securityManager.verifySignedData(signature, cert, archive));
    }

    @Test
    public void verifySignedDataTestCertWrongIntermediate() throws IOException, URISyntaxException, SecurityManagerException {
        Assertions.assertThrows(SecurityManagerException.class, () -> {
            PrepareCertFiles("/cert/root.cert", cerDirPath + "root.cert");
            PrepareCertFiles("/cert/signing-ca1.crt", cerDirPath + "signing-ca1.crt");
            byte[] signature = readAllBytes("/cert/3-file-signed-package/dummyPnfv4.cms");
            byte[] archive = readAllBytes("/cert/3-file-signed-package/dummyPnfv4.csar");
            byte[] cert = readAllBytes("/cert/3-file-signed-package/dummyPnfv4-no-intermediate.cert");
            securityManager.verifySignedData(signature, cert, archive);
        });

    }

    @Test
    public void verifySignedDataTestCertIncludedIntoSignatureWithWrongIntermediateInDirectory() throws IOException, URISyntaxException, SecurityManagerException {
        PrepareCertFiles("/cert/rootCA.cert", cerDirPath + "root.cert");
        PrepareCertFiles("/cert/signing-ca1.crt", cerDirPath + "signing-ca1.crt");
        byte[] signature = readAllBytes("/cert/2-file-signed-package/dummyPnfv4.cms");
        byte[] archive = readAllBytes("/cert/2-file-signed-package/dummyPnfv4.csar");
        assertTrue(securityManager.verifySignedData(signature, null, archive));
    }

    @Test
    public void verifySignedDataTestCertWrongIntermediateInDirectory() throws IOException, URISyntaxException, SecurityManagerException {
        PrepareCertFiles("/cert/rootCA.cert", cerDirPath + "root.cert");
        PrepareCertFiles("/cert/signing-ca1.crt", cerDirPath + "signing-ca1.crt");
        byte[] signature = readAllBytes("/cert/3-file-signed-package/dummyPnfv4.cms");
        byte[] archive = readAllBytes("/cert/3-file-signed-package/dummyPnfv4.csar");
        byte[] cert = readAllBytes("/cert/3-file-signed-package/dummyPnfv4.cert");
        assertTrue(securityManager.verifySignedData(signature, cert, archive));
    }

    @Test
    public void verifySignedDataTestWrongCertificate() throws IOException, URISyntaxException, SecurityManagerException {
        Assertions.assertThrows(SecurityManagerException.class, () -> {
            PrepareCertFiles("/cert/root-certificate.pem", cerDirPath + "root-certificate.cert");
            byte[] signature = readAllBytes("/cert/3-file-signed-package/dummyPnfv4.cms");
            byte[] archive = readAllBytes("/cert/3-file-signed-package/dummyPnfv4.csar");
            byte[] cert = readAllBytes("/cert/3-file-signed-package/dummyPnfv4.cert");
            securityManager.verifySignedData(signature, cert, archive);
        });

    }

    @Test
    public void verifySignedDataTestChangedArchive() throws IOException, URISyntaxException, SecurityManagerException {
        Assertions.assertThrows(SecurityManagerException.class, () -> {
            PrepareCertFiles("/cert/root.cert", cerDirPath + "root.cert");
            byte[] signature = readAllBytes("/cert/tampered-signed-package/dummyPnfv4.cms");
            byte[] archive = readAllBytes("/cert/tampered-signed-package/dummyPnfv4.csar");
            securityManager.verifySignedData(signature, null, archive);
        });

    }
}
