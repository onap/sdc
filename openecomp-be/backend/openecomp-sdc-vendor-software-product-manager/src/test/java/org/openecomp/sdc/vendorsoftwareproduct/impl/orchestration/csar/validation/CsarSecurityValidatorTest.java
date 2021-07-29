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
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.openecomp.sdc.be.csar.storage.ArtifactInfo;
import org.openecomp.sdc.be.csar.storage.PersistentStorageArtifactInfo;
import org.openecomp.sdc.vendorsoftwareproduct.impl.onboarding.OnboardingPackageProcessor;
import org.openecomp.sdc.vendorsoftwareproduct.impl.onboarding.validation.CnfPackageValidator;
import org.openecomp.sdc.vendorsoftwareproduct.security.SecurityManager;
import org.openecomp.sdc.vendorsoftwareproduct.security.SecurityManagerException;
import org.openecomp.sdc.vendorsoftwareproduct.types.OnboardPackageInfo;
import org.openecomp.sdc.vendorsoftwareproduct.types.OnboardSignedPackage;

class CsarSecurityValidatorTest {

    private static final String BASE_DIR = "/vspmanager.csar/signing/";
    private static final String DELIMITER = "---";
    private CsarSecurityValidator csarSecurityValidator;
    @Mock
    private SecurityManager securityManager;

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
        initMocks(this);
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
    void isSignatureValidTestCorrectStructureAndValidSignatureExists() throws SecurityManagerException, IOException {
        final byte[] packageBytes = getFileBytesOrFail("signed-package.zip");
        final OnboardPackageInfo onboardPackageInfo = loadSignedPackageWithArtifactInfo("signed-package.zip", packageBytes, null);
        when(securityManager.verifyPackageSignedData(any(OnboardSignedPackage.class), any(ArtifactInfo.class))).thenReturn(true);
        final boolean isSignatureValid = csarSecurityValidator
            .verifyPackageSignature((OnboardSignedPackage) onboardPackageInfo.getOriginalOnboardPackage(), onboardPackageInfo.getArtifactInfo());
        assertThat("Signature should be valid", isSignatureValid, is(true));
    }

    @Test
    void isSignatureValidTestCorrectStructureAndNotValidSignatureExists() throws SecurityManagerException {
        final byte[] packageBytes = getFileBytesOrFail("signed-package-tampered-data.zip");
        final OnboardPackageInfo onboardPackageInfo = loadSignedPackageWithArtifactInfo("signed-package-tampered-data.zip", packageBytes, null);
        //no mocked securityManager
        csarSecurityValidator = new CsarSecurityValidator();
        Assertions.assertThrows(SecurityManagerException.class, () -> {
            csarSecurityValidator
                .verifyPackageSignature((OnboardSignedPackage) onboardPackageInfo.getOriginalOnboardPackage(), onboardPackageInfo.getArtifactInfo());
        });
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

    private OnboardPackageInfo loadSignedPackageWithArtifactInfo(final String packageName, final byte[] packageBytes,
                                                                 final CnfPackageValidator cnfPackageValidator) {
        final OnboardingPackageProcessor onboardingPackageProcessor =
            new OnboardingPackageProcessor(packageName, packageBytes, cnfPackageValidator,
                new PersistentStorageArtifactInfo(Path.of("src/test/resources/vspmanager.csar/signing/signed-package.zip")));
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
