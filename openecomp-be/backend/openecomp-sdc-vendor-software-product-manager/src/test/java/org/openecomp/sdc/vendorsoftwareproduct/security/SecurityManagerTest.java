package org.openecomp.sdc.vendorsoftwareproduct.security;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.io.IOException;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.mockito.ArgumentMatchers.eq;

@RunWith(PowerMockRunner.class)
@PrepareForTest(SecurityManager.class)
public class SecurityManagerTest {
    File certDir;

    @Before
    public void setUp(){
        certDir = new File("/tmp/cert");
        certDir.mkdirs();
        PowerMockito.mockStatic(System.class);
        PowerMockito.when(System.getenv(eq("SDC_CERT_DIR"))).thenReturn(certDir.getPath());
    }

    @After
    public void tearDown(){
        certDir.delete();
    }

    @Test
    public void testGetCertificates() throws IOException {
        File origFile = new File("src/test/resources/cert/root-certificate.pem");
        File newFile = new File("/tmp/cert/root-certificate.pem");
        newFile.createNewFile();
        FileUtils.copyFile(origFile, newFile);
        SecurityManager securityManager = new SecurityManager();
        assertEquals(1, securityManager.getCertificates().size());
        newFile.delete();
        assertEquals(0, securityManager.getCertificates().size());
    }

    @Test
    public void testGetCertificatesNoDirectory() throws IOException {
        certDir.delete();
        SecurityManager securityManager = new SecurityManager();
        assertEquals(0, securityManager.getCertificates().size());
    }

    @Test(expected = SecurityManagerException.class)
    public void testGetCertificatesException() throws IOException {
        File newFile = new File("/tmp/cert/root-certificate.pem");
        newFile.createNewFile();
        SecurityManager securityManager = new SecurityManager();
        assertEquals(1, securityManager.getCertificates().size());
        newFile.delete();
        assertEquals(0, securityManager.getCertificates().size());
    }

    @Test
    public void testGetCertificatesUpdated() throws IOException {
        File origFile = new File("src/test/resources/cert/root-certificate.pem");
        File newFile = new File("/tmp/cert/root-certificate.pem");
        newFile.createNewFile();
        FileUtils.copyFile(origFile, newFile);
        SecurityManager securityManager = new SecurityManager();
        assertTrue(securityManager.getCertificates().size() == 1);
        File otherOrigFile = new File("src/test/resources/cert/package-certificate.pem");
        File otherNewFile = new File("/tmp/cert/package-certificate.pem");
        newFile.createNewFile();
        FileUtils.copyFile(otherOrigFile, otherNewFile);
        assertEquals(2, securityManager.getCertificates().size());
        otherNewFile.delete();
        assertEquals(1, securityManager.getCertificates().size());
        newFile.delete();
        assertEquals(0, securityManager.getCertificates().size());
    }
}
