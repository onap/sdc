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
import java.security.Key;
import java.security.Security;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openecomp.sdc.be.csar.security.exception.LoadPrivateKeyException;
import org.openecomp.sdc.be.csar.security.exception.UnsupportedKeyFormatException;

class PrivateKeyReaderImplTest {

    private PrivateKeyReaderImpl privateKeyReader;

    @BeforeEach
    void setUp() {
        privateKeyReader = new PrivateKeyReaderImpl();
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    @AfterEach
    void tearDown() {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) != null) {
            Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
        }
    }

    @Test
    void loadPrivateKeySuccessTest() {
        final Path certPath = Paths.get("certificateManager", "realCert", "realCert1.key");
        final URL resource = getClass().getClassLoader().getResource(certPath.toString());
        if (resource == null) {
            fail("Could not find resource " + certPath.toString());
        }
        final Key privateKey = privateKeyReader.loadPrivateKey(new File(resource.getPath()));
        assertNotNull(privateKey);
    }

    @Test
    void loadInvalidKeyFilePathTest() {
        final String invalidFilePath = "aaaa";
        final File keyFile = new File(invalidFilePath);
        final LoadPrivateKeyException actualException = assertThrows(LoadPrivateKeyException.class,
            () -> privateKeyReader.loadPrivateKey(keyFile));
        assertThat(actualException.getMessage(),
            is(String.format("Could not load the private key from given file '%s'", invalidFilePath)));
    }

    @Test
    void loadInvalidKeyFileTest() {
        final Path certPath = Paths.get("certificateManager", "fakeCert1.key");
        final URL resource = getClass().getClassLoader().getResource(certPath.toString());
        if (resource == null) {
            fail("Could not find resource " + certPath.toString());
        }
        final File keyFile = new File(resource.getPath());
        final UnsupportedKeyFormatException actualException = assertThrows(UnsupportedKeyFormatException.class,
            () -> privateKeyReader.loadPrivateKey(keyFile));
        assertThat(actualException.getMessage(),
            is(String.format("Could not load the private key from given file '%s'. Unsupported format.",
                resource.getPath())));
    }
}