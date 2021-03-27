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
import java.io.FileReader;
import java.security.Key;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.openecomp.sdc.be.csar.security.api.PrivateKeyReader;
import org.openecomp.sdc.be.csar.security.exception.LoadPrivateKeyException;
import org.openecomp.sdc.be.csar.security.exception.UnsupportedKeyFormatException;
import org.springframework.stereotype.Component;

@Component
public class PrivateKeyReaderImpl implements PrivateKeyReader {

    @Override
    public Key loadPrivateKey(final File privateKeyFile) {
        try (final PEMParser pemParser = new PEMParser(new FileReader(privateKeyFile))) {
            final Object pemObject = pemParser.readObject();
            final JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider(BouncyCastleProvider.PROVIDER_NAME);
            if (pemObject instanceof PrivateKeyInfo) {
                return converter.getPrivateKey((PrivateKeyInfo) pemObject);
            }
        } catch (final Exception e) {
            final String errorMsg = "Could not load the private key from given file '%s'";
            throw new LoadPrivateKeyException(String.format(errorMsg, privateKeyFile), e);
        }
        final String errorMsg = "Could not load the private key from given file '%s'. Unsupported format.";
        throw new UnsupportedKeyFormatException(String.format(errorMsg, privateKeyFile));
    }
}
