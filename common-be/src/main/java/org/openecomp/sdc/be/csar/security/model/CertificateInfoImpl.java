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

import java.io.File;
import java.security.Key;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import lombok.Getter;
import org.apache.commons.io.FilenameUtils;
import org.openecomp.sdc.be.csar.security.api.model.CertificateInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Getter
public class CertificateInfoImpl implements CertificateInfo {

    private static final Logger LOGGER = LoggerFactory.getLogger(CertificateInfoImpl.class);

    private final String name;
    private final File certificateFile;
    private final Certificate certificate;
    private File privateKeyFile;
    private Key privateKey;

    public CertificateInfoImpl(final File certificateFile, final Certificate certificate) {
        this.certificateFile = certificateFile;
        this.certificate = certificate;
        this.name = FilenameUtils.getBaseName(certificateFile.getName());
    }

    public CertificateInfoImpl(final File certificateFile, final Certificate certificate,
                               final File privateKeyFile, final Key privateKey) {
        this(certificateFile, certificate);
        this.privateKeyFile = privateKeyFile;
        this.privateKey = privateKey;
    }

    @Override
    public boolean isValid() {
        if("X.509".equals(certificate.getType())) {
            try {
                ((X509Certificate) certificate).checkValidity();
                return true;
            } catch (final Exception e) {
                LOGGER.warn("Invalid certificate '{}'", certificateFile.getAbsolutePath(), e);
                return false;
            }
        }
        throw new UnsupportedOperationException(String.format("Certificate type '%s' not supported", certificate.getType()));
    }

}
