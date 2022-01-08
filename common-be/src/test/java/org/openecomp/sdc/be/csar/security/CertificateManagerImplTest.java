/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nordix Foundation
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

package org.openecomp.sdc.be.csar.security;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.openecomp.sdc.be.csar.security.CertificateManagerImpl.CERT_DIR_ENV_VARIABLE;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.csar.security.api.CertificateReader;
import org.openecomp.sdc.be.csar.security.api.PrivateKeyReader;
import org.openecomp.sdc.be.csar.security.api.model.CertificateInfo;
import org.springframework.core.env.Environment;

class CertificateManagerImplTest {

    @Mock
    private Environment environment;
    @Mock
    private PrivateKeyReader privateKeyReader;
    @Mock
    private CertificateReader certificateReader;
    @Mock
    private X509Certificate certificateMock;
    private CertificateManagerImpl certificateManager;

    static Path certificateFolderPath;

    @BeforeAll
    static void beforeAll() {
        final String resourceFolder = "certificateManager";
        final URL certificateManager = CertificateManagerImplTest.class.getClassLoader().getResource(resourceFolder);
        if (certificateManager == null) {
            fail("Could not find resource folder " + resourceFolder);
        }
        certificateFolderPath = Paths.get(certificateManager.getPath());
    }

    @BeforeEach
    void setUp() throws CertificateNotYetValidException, CertificateExpiredException {
        MockitoAnnotations.openMocks(this);
        when(environment.getProperty(CERT_DIR_ENV_VARIABLE)).thenReturn(certificateFolderPath.toString());
        when(certificateMock.getType()).thenReturn("X.509");
        doNothing().when(certificateMock).checkValidity();
        when(certificateReader.loadCertificate(ArgumentMatchers.any())).thenReturn(certificateMock);
        certificateManager = new CertificateManagerImpl(privateKeyReader, certificateReader, environment);
    }

    @Test
    void getCertificateSuccessTest() {
        final String certificateName = "fakeCert1";
        final Optional<CertificateInfo> certificateOpt = certificateManager.getCertificate(certificateName);
        assertThat(certificateOpt.isPresent(), is(true));
        final CertificateInfo certificateInfo = certificateOpt.get();
        assertThat(certificateInfo.getName(), is(certificateName));
        assertThat(certificateInfo.getPrivateKeyFile(), is(notNullValue()));
        assertThat(certificateInfo.getPrivateKeyFile().getAbsolutePath(),
            is(certificateFolderPath.resolve(certificateName + ".key").toString()));
        assertThat(certificateInfo.getCertificateFile(), is(notNullValue()));
        assertThat(certificateInfo.getCertificateFile().getAbsolutePath(),
            is(certificateFolderPath.resolve(certificateName + ".cert").toString()));
    }

    @Test
    void initCertificateSuccessTest() {
        final String certificateName1 = "fakeCert1";
        final String certificateName2 = "fakeCert2";
        final String certificateName3 = "fakeCert3";
        assertThat("Certificate " + certificateName1 + " should be present",
            certificateManager.getCertificate(certificateName1).isPresent(), is(true));
        assertThat("Certificate " + certificateName2 + " should be present",
            certificateManager.getCertificate(certificateName2).isPresent(), is(true));
        assertThat("Certificate " + certificateName3 + " should not be present",
            certificateManager.getCertificate(certificateName3).isEmpty(), is(true));
    }

    @Test
    void invalidCertificateFolderTest() {
        final String certificateName1 = "fakeCert1";
        when(environment.getProperty(CERT_DIR_ENV_VARIABLE)).thenReturn("/an/invalid/folder");
        final CertificateManagerImpl certificateManager =
            new CertificateManagerImpl(privateKeyReader, certificateReader, environment);
        assertThat("Certificate " + certificateName1 + " should be present",
            certificateManager.getCertificate(certificateName1).isPresent(), is(false));
    }

    @Test
    void noEnvironmentVariableConfiguredTest() {
        final String certificateName1 = "fakeCert1";
        when(environment.getProperty(CERT_DIR_ENV_VARIABLE)).thenReturn(null);
        final CertificateManagerImpl certificateManager =
            new CertificateManagerImpl(privateKeyReader, certificateReader, environment);
        assertThat("Certificate " + certificateName1 + " should be present",
            certificateManager.getCertificate(certificateName1).isPresent(), is(false));
    }

    @Test
    void loadCertificateExceptionTest() {
        final String certificateName1 = "fakeCert1";
        when(certificateReader.loadCertificate(any())).thenThrow(new RuntimeException());
        final CertificateManagerImpl certificateManager =
            new CertificateManagerImpl(privateKeyReader, certificateReader, environment);
        assertThat("Certificate " + certificateName1 + " should be present",
            certificateManager.getCertificate(certificateName1).isPresent(), is(false));
    }

}