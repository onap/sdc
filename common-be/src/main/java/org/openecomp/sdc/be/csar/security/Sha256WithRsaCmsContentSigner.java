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

import java.io.IOException;
import java.io.StringWriter;
import java.security.Key;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.cms.ContentInfo;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.CMSTypedData;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.openecomp.sdc.be.csar.security.api.CmsContentSigner;
import org.openecomp.sdc.be.csar.security.exception.CmsSignatureException;
import org.springframework.stereotype.Component;

@Component
public class Sha256WithRsaCmsContentSigner implements CmsContentSigner {

    @Override
    public byte[] signData(final byte[] data, final Certificate signingCertificate, final Key signingKey)
        throws CmsSignatureException {

        final CMSTypedData cmsData = new CMSProcessableByteArray(data);
        final JcaCertStore certStore = createCertificateStore(signingCertificate);
        try {
            final ContentSigner contentSigner
                = new JcaContentSignerBuilder("SHA256withRSA")
                .setProvider(BouncyCastleProvider.PROVIDER_NAME).build((PrivateKey) signingKey);

            final CMSSignedDataGenerator cmsGenerator = new CMSSignedDataGenerator();
            cmsGenerator.addSignerInfoGenerator(
                new JcaSignerInfoGeneratorBuilder(
                    new JcaDigestCalculatorProviderBuilder().setProvider(BouncyCastleProvider.PROVIDER_NAME).build()
                ).build(contentSigner, (X509Certificate) signingCertificate)
            );
            cmsGenerator.addCertificates(certStore);

            final CMSSignedData cms = cmsGenerator.generate(cmsData, false);
            return cms.getEncoded();
        } catch (final Exception e) {
            throw new CmsSignatureException("Could not sign the given data", e);
        }
    }

    @Override
    public String formatToPemSignature(final byte[] signedData) throws CmsSignatureException {
        final StringWriter sw = new StringWriter();
        try (final JcaPEMWriter jcaPEMWriter = new JcaPEMWriter(sw)) {
            final ContentInfo ci = ContentInfo.getInstance(ASN1Primitive.fromByteArray(signedData));
            jcaPEMWriter.writeObject(ci);
        } catch (final IOException e) {
            throw new CmsSignatureException("Could not convert signed data to PEM format", e);
        }
        return sw.toString();
    }

    private JcaCertStore createCertificateStore(final Certificate signingCertificate) throws CmsSignatureException {
        try {
            return new JcaCertStore(Collections.singletonList(signingCertificate));
        } catch (final CertificateEncodingException e) {
            final String errorMsg = String
                .format("Could not create certificate store from certificate '%s'", signingCertificate);
            throw new CmsSignatureException(errorMsg, e);
        }
    }

}
