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

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

public class SecurityManagerTest {
    private File certDir;
    private SecurityManager securityManager;

    @Before
    public void setUp() throws IOException {
        certDir = new File("/tmp/cert");
        if(certDir.exists()){
            tearDown();
        }
        certDir.mkdirs();
        securityManager = new SecurityManager(certDir.getPath());
    }

    @After
    public void tearDown() throws IOException {
        if(certDir.exists()) {
            FileUtils.deleteDirectory(certDir);
        }
        securityManager.cleanTrustedCertificates();
    }

    @Test
    public void testGetCertificates() throws IOException, SecurityManagerException {
        File origFile = new File("src/test/resources/cert/root-certificate.pem");
        File newFile = new File("/tmp/cert/root-certificate.pem");
        newFile.createNewFile();
        FileUtils.copyFile(origFile, newFile);
        assertEquals(1, securityManager.getTrustedCertificates().size());
        newFile.delete();
        assertEquals(0, securityManager.getTrustedCertificates().size());
    }

    @Test
    public void testGetCertificatesNoDirectory() throws IOException, SecurityManagerException {
        certDir.delete();
        assertEquals(0, securityManager.getTrustedCertificates().size());
    }

    @Test(expected = SecurityManagerException.class)
    public void testGetCertificatesException() throws IOException, SecurityManagerException {
        File newFile = new File("/tmp/cert/root-certificate.pem");
        newFile.createNewFile();
        assertEquals(1, securityManager.getTrustedCertificates().size());
        newFile.delete();
        assertEquals(0, securityManager.getTrustedCertificates().size());
    }

    @Test
    public void testGetCertificatesUpdated() throws IOException, SecurityManagerException {
        File origFile = new File("src/test/resources/cert/root-certificate.pem");
        File newFile = new File("/tmp/cert/root-certificate.pem");
        newFile.createNewFile();
        FileUtils.copyFile(origFile, newFile);
        assertTrue(securityManager.getTrustedCertificates().size() == 1);
        File otherOrigFile = new File("src/test/resources/cert/package-certificate.pem");
        File otherNewFile = new File("/tmp/cert/package-certificate.pem");
        newFile.createNewFile();
        FileUtils.copyFile(otherOrigFile, otherNewFile);
        assertEquals(2, securityManager.getTrustedCertificates().size());
        otherNewFile.delete();
        assertEquals(1, securityManager.getTrustedCertificates().size());
        newFile.delete();
        assertEquals(0, securityManager.getTrustedCertificates().size());
    }

    @Test
    public void verifySignedDataTestCertIncludedIntoSignature() throws IOException, URISyntaxException, SecurityManagerException {
        File origFile = new File("src/test/resources/cert/root.cert");
        File newFile = new File("/tmp/cert/root.cert");
        newFile.createNewFile();
        FileUtils.copyFile(origFile, newFile);
        byte[] signature = Files.readAllBytes(Paths.get(getClass().getResource("/cert/2-file-signed-package/dummyPnfv4.cms").toURI()));
        byte[] archive = Files.readAllBytes(Paths.get(getClass().getResource("/cert/2-file-signed-package/dummyPnfv4.csar").toURI()));
        assertTrue(securityManager.verifySignedData(signature, null, archive));
    }

    @Test(expected = SecurityManagerException.class)
    public void verifySignedDataTestCertNotIncludedIntoSignatureButExpected() throws IOException, URISyntaxException, SecurityManagerException {
        File origFile = new File("src/test/resources/cert/root.cert");
        File newFile = new File("/tmp/cert/root.cert");
        newFile.createNewFile();
        FileUtils.copyFile(origFile, newFile);
        byte[] signature = Files.readAllBytes(Paths.get(getClass().getResource("/cert/3-file-signed-package/dummyPnfv4.cms").toURI()));
        byte[] archive = Files.readAllBytes(Paths.get(getClass().getResource("/cert/2-file-signed-package/dummyPnfv4.csar").toURI()));
        securityManager.verifySignedData(signature, null, archive);
    }

    @Test
    public void verifySignedDataTestCertNotIncludedIntoSignature() throws IOException, URISyntaxException, SecurityManagerException {
        File origFile = new File("src/test/resources/cert/root.cert");
        File newFile = new File("/tmp/cert/root.cert");
        newFile.createNewFile();
        FileUtils.copyFile(origFile, newFile);
        byte[] signature = Files.readAllBytes(Paths.get(getClass().getResource("/cert/3-file-signed-package/dummyPnfv4.cms").toURI()));
        byte[] archive = Files.readAllBytes(Paths.get(getClass().getResource("/cert/3-file-signed-package/dummyPnfv4.csar").toURI()));
        byte[] cert = Files.readAllBytes(Paths.get(getClass().getResource("/cert/3-file-signed-package/dummyPnfv4.cert").toURI()));
        assertTrue(securityManager.verifySignedData(signature, cert, archive));
    }

    @Test(expected = SecurityManagerException.class)
    public void verifySignedDataTestWrongCertificate() throws IOException, URISyntaxException, SecurityManagerException {
        File origFile = new File("src/test/resources/cert/root-certificate.pem");
        File newFile = new File("/tmp/cert/root-certificate.cert");
        newFile.createNewFile();
        FileUtils.copyFile(origFile, newFile);
        byte[] signature = Files.readAllBytes(Paths.get(getClass().getResource("/cert/3-file-signed-package/dummyPnfv4.cms").toURI()));
        byte[] archive = Files.readAllBytes(Paths.get(getClass().getResource("/cert/3-file-signed-package/dummyPnfv4.csar").toURI()));
        byte[] cert = Files.readAllBytes(Paths.get(getClass().getResource("/cert/3-file-signed-package/dummyPnfv4.cert").toURI()));
        securityManager.verifySignedData(signature, cert, archive);
    }

    @Test(expected = SecurityManagerException.class)
    public void verifySignedDataTestChangedArchive() throws IOException, URISyntaxException, SecurityManagerException {
        File origFile = new File("src/test/resources/cert/root.cert");
        File newFile = new File("/tmp/cert/root.cert");
        newFile.createNewFile();
        FileUtils.copyFile(origFile, newFile);
        byte[] signature = Files.readAllBytes(Paths.get(getClass().getResource("/cert/tampered-signed-package/dummyPnfv4.cms").toURI()));
        byte[] archive = Files.readAllBytes(Paths.get(getClass().getResource("/cert/tampered-signed-package/dummyPnfv4.csar").toURI()));
        securityManager.verifySignedData(signature, null, archive);
    }
}
