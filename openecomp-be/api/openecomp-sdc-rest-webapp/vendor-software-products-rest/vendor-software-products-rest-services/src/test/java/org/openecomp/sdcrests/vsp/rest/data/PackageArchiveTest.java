package org.openecomp.sdcrests.vsp.rest.data;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.openecomp.sdc.vendorsoftwareproduct.security.SecurityManager;
import org.powermock.reflect.Whitebox;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.when;

public class PackageArchiveTest {
    private static final String BASE_DIR = "/vspmanager.csar/";

    @Mock
    SecurityManager manager;

    @Before
    public void setUp(){
        initMocks(this);
    }


    @Test
    public void isSignedTestCheckingWrongFile() throws IOException,
            URISyntaxException {
        PackageArchive packageArchive = getArchive("notCsar.txt");
        assertFalse("2 or 3 files expected for signed package present or signature valid for " +
                "empty file", packageArchive.isSigned());
    }

    @Test
    public void isSignedTestWrongPackageStructure2EmptyDirInRoot() throws IOException,
            URISyntaxException {
        PackageArchive packageArchive = getArchive("signing/2-empty-directories-in-root.zip");
        assertFalse(packageArchive.isSigned());
    }

    @Test
    public void isSignedTestWrongPackageStructure2EmptyFilesAndEmptyDirInRoot() throws IOException,
            URISyntaxException {
        PackageArchive packageArchive = getArchive("signing/2-empty-files-1-empty-directory-in-root.zip");
        assertFalse(packageArchive.isSigned());
    }

    @Test
    public void isSignedTestWrongPackageStructure2EmptyFilesAndDirWithContentInRoot() throws IOException,
            URISyntaxException {
        PackageArchive packageArchive = getArchive("signing/2-empty-files-1-directory-with-contents-in-root.zip");
        assertFalse(packageArchive.isSigned());
    }

    @Test
    public void isSignedTestCorrectStructureNoSignature() throws IOException,
            URISyntaxException {
        PackageArchive packageArchive = getArchive("signing/2-files-in-root.zip");
        assertFalse(packageArchive.isSigned());
    }

    @Test
    public void isSignedTestCorrectStructureAndSignatureExists() throws IOException,
            URISyntaxException {
        PackageArchive packageArchive = getArchive("signing/csar-and-cms-in-root.zip");
        assertTrue(packageArchive.isSigned());
    }

    @Test
    public void isSignatureValidTestCorrectStructureAndValidSignatureExists() throws IOException,
            URISyntaxException {
        PackageArchive packageArchive = getArchive("signing/signed-package.zip");
        Whitebox.setInternalState(packageArchive, "securityManager", manager);
        when(manager.verifySignedData(any(), any(), any())).thenReturn(true);
        assertTrue("Signature invalid for signed package",
                packageArchive.isSignatureValid());
    }

    @Test
    public void isSignatureValidTestCorrectStructureAndNotValidSignatureExists() throws IOException,
            URISyntaxException {
        PackageArchive packageArchive = getArchive("signing/signed-package-tampered-data.zip");
        Whitebox.setInternalState(packageArchive, "securityManager", manager);
        when(manager.verifySignedData(any(), any(), any())).thenReturn(false);
        assertFalse("Signature valid for signed package that has modified data instead of " +
                "original data", packageArchive.isSignatureValid());
    }

    private PackageArchive getArchive(String path) throws URISyntaxException, IOException {
        return new PackageArchive(Files.readAllBytes(Paths.get(
                PackageArchiveTest.class.getResource(BASE_DIR + path).toURI())));
    }
}
