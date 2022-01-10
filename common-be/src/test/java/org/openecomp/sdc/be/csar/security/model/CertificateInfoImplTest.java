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

package org.openecomp.sdc.be.csar.security.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.io.File;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class CertificateInfoImplTest {

    @Mock
    private X509Certificate certificate;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void isValidTest() throws CertificateNotYetValidException, CertificateExpiredException {
        when(certificate.getType()).thenReturn("X.509");
        doNothing().when(certificate).checkValidity();
        final CertificateInfoImpl certificateInfo = new CertificateInfoImpl(new File(""), certificate);
        assertTrue(certificateInfo.isValid());
        doThrow(CertificateExpiredException.class).when(certificate).checkValidity();
        assertFalse(certificateInfo.isValid());
    }

    @Test
    void unsupportedCertificateTypeTest() {
        final String certificateType = "unknown";
        when(certificate.getType()).thenReturn(certificateType);
        final CertificateInfoImpl certificateInfo = new CertificateInfoImpl(new File(""), certificate);
        final UnsupportedOperationException actualException =
            assertThrows(UnsupportedOperationException.class, certificateInfo::isValid);
        assertEquals(actualException.getMessage(),
            String.format("Certificate type '%s' not supported", certificateType));
    }
}
