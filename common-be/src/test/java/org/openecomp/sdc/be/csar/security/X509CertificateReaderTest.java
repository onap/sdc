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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.cert.Certificate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openecomp.sdc.be.csar.security.exception.LoadCertificateException;

class X509CertificateReaderTest {

    private X509CertificateReader certificateReader;

    @BeforeEach
    void setUp() {
        certificateReader = new X509CertificateReader();
    }

    @Test
    void loadCertificateSuccessTest() {
        final Path certPath = Paths.get("certificateManager", "realCert", "realCert1.cert");
        final URL resource = getClass().getClassLoader().getResource(certPath.toString());
        if (resource == null) {
            fail("Could not find resource " + certPath.toString());
        }
        final Certificate certificate = certificateReader.loadCertificate(new File(resource.getPath()));
        assertNotNull(certificate);
    }

    @Test
    void loadInvalidCertificateFilePathTest() {
        final String invalidFilePath = "aaaa";
        final File certFile = new File(invalidFilePath);
        final LoadCertificateException actualException = assertThrows(LoadCertificateException.class,
            () -> certificateReader.loadCertificate(certFile));
        assertThat(actualException.getMessage(),
            is(String.format("Could not load the certificate from given file '%s'", invalidFilePath)));
    }

    @Test
    void loadInvalidCertificateFileTest() {
        final Path certPath = Paths.get("certificateManager", "fakeCert1.cert");
        System.out.println(certPath.toString());
        final URL resource = getClass().getClassLoader().getResource(certPath.toString());
        if (resource == null) {
            fail("Could not find resource " + certPath.toString());
        }
        final File certFile = new File(resource.getPath());
        final LoadCertificateException actualException = assertThrows(LoadCertificateException.class,
            () -> certificateReader.loadCertificate(certFile));
        assertThat(actualException.getMessage(),
            is(String.format("Could not load the certificate from given file '%s'", resource.getPath())));
    }
}