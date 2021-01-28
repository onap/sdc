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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import org.openecomp.sdc.be.csar.security.api.CertificateReader;
import org.openecomp.sdc.be.csar.security.exception.LoadCertificateException;
import org.springframework.stereotype.Component;

@Component
public class X509CertificateReader implements CertificateReader {

    /**
     * Reads X.509 certificate file.
     *
     * @param certFile the certificate file
     * @return the read certificate
     * @throws LoadCertificateException when an error has occurred while reading the certificate
     */
    @Override
    public Certificate loadCertificate(final File certFile) {
        try (final FileInputStream fi = new FileInputStream(certFile)) {
            return buildCertificate(fi);
        } catch (final Exception e) {
            final String errorMsg = "Could not load the certificate from given file '%s'";
            throw new LoadCertificateException(String.format(errorMsg, certFile), e);
        }
    }

    private Certificate buildCertificate(final InputStream certificateInputStream) throws CertificateException {
        final CertificateFactory factory = CertificateFactory.getInstance("X.509");
        return factory.generateCertificate(certificateInputStream);
    }

}
