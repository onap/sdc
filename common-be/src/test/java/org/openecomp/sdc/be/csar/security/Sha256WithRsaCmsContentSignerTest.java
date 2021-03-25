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
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Key;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationStore;
import org.bouncycastle.cms.SignerInformationVerifier;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openecomp.sdc.be.csar.security.api.CertificateReader;
import org.openecomp.sdc.be.csar.security.api.PrivateKeyReader;
import org.openecomp.sdc.be.csar.security.exception.CmsSignatureException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

class Sha256WithRsaCmsContentSignerTest {

    private Sha256WithRsaCmsContentSigner cmsContentSigner;
    private PrivateKeyReader privateKeyReader;
    private CertificateReader certificateReader;

    private static final Path testFilesPath = Path.of("certificateManager", "signerTest");
    private static final Path certFilesPath = Path.of("certificateManager", "realCert");

    @BeforeEach
    void setUp() {
        Security.addProvider(new BouncyCastleProvider());
        cmsContentSigner = new Sha256WithRsaCmsContentSigner();
        privateKeyReader = new PrivateKeyReaderImpl();
        certificateReader = new X509CertificateReader();
    }

    @AfterEach
    void tearDown() {
        Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
    }

    @Test
    void signDataSuccessTest() throws OperatorCreationException, CMSException, IOException, CmsSignatureException {
        final File certFile = getResourceFile(certFilesPath.resolve("realCert1.cert"));
        final File keyFile = getResourceFile(certFilesPath.resolve("realCert1.key"));
        final File fileToSign = getResourceFile(testFilesPath.resolve("fileToSign.txt"));
        final Key privateKey = privateKeyReader.loadPrivateKey(keyFile);
        final Certificate certificate = certificateReader.loadCertificate(certFile);
        final byte[] actualSignatureBytes = cmsContentSigner
            .signData(Files.readAllBytes(fileToSign.toPath()), certificate, privateKey);

        assertTrue(verifySignature(Files.readAllBytes(fileToSign.toPath()), actualSignatureBytes,
            (X509Certificate) certificate));
    }

    @Test
    void formatToPemSignatureTest() throws OperatorCreationException, CMSException, IOException, CmsSignatureException {
        final File certFile = getResourceFile(certFilesPath.resolve("realCert1.cert"));
        final File keyFile = getResourceFile(certFilesPath.resolve("realCert1.key"));
        final File fileToSign = getResourceFile(testFilesPath.resolve("fileToSign.txt"));
        final Key privateKey = privateKeyReader.loadPrivateKey(keyFile);
        final Certificate certificate = certificateReader.loadCertificate(certFile);
        final byte[] actualSignatureBytes = cmsContentSigner
                .signData(Files.readAllBytes(fileToSign.toPath()), certificate, privateKey);

        assertNotNull(cmsContentSigner.formatToPemSignature(actualSignatureBytes));
        assertThrows(CmsSignatureException.class,
                () -> cmsContentSigner.formatToPemSignature(new byte[10]));
    }

    @Test
    void signDataInvalidCertAndKeyTest() {
        assertThrows(CmsSignatureException.class,
            () -> cmsContentSigner.signData(null, null, null));
    }

    private boolean verifySignature(byte[] contentBytes, byte[] signatureBytes, X509Certificate certificate)
        throws CMSException, OperatorCreationException {

        final CMSSignedData cms = new CMSSignedData(new CMSProcessableByteArray(contentBytes), signatureBytes);
        final SignerInformationStore signers = cms.getSignerInfos();
        final SignerInformationVerifier signerInformationVerifier =
            new JcaSimpleSignerInfoVerifierBuilder()
                .setProvider(BouncyCastleProvider.PROVIDER_NAME).build(certificate);
        for (final SignerInformation signer : signers.getSigners()) {
            if (!signer.verify(signerInformationVerifier)) {
                return false;
            }
        }

        return true;
    }

    private File getResourceFile(final Path testResourcePath) {
        final URL resource = getClass().getClassLoader().getResource(testResourcePath.toString());
        if (resource == null) {
            fail("Could not load the file " + testResourcePath.toString());
        }

        return new File(resource.getPath());
    }

}