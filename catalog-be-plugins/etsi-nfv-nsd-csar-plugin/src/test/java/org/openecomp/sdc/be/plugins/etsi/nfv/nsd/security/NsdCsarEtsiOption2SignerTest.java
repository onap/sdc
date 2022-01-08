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

package org.openecomp.sdc.be.plugins.etsi.nfv.nsd.security;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.openecomp.sdc.be.plugins.etsi.nfv.nsd.security.NsdCsarEtsiOption2Signer.SDC_NSD_CERT_NAME;
import static org.openecomp.sdc.be.plugins.etsi.nfv.nsd.security.NsdCsarEtsiOption2Signer.SIGNATURE_EXTENSION;
import static org.openecomp.sdc.be.plugins.etsi.nfv.nsd.security.exception.NsdSignatureExceptionSupplier.certificateNotConfigured;
import static org.openecomp.sdc.be.plugins.etsi.nfv.nsd.security.exception.NsdSignatureExceptionSupplier.invalidCertificate;
import static org.openecomp.sdc.be.plugins.etsi.nfv.nsd.security.exception.NsdSignatureExceptionSupplier.unableToCreateSignature;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.csar.security.api.CertificateManager;
import org.openecomp.sdc.be.csar.security.api.CmsContentSigner;
import org.openecomp.sdc.be.csar.security.api.model.CertificateInfo;
import org.openecomp.sdc.be.csar.security.exception.CmsSignatureException;
import org.openecomp.sdc.be.plugins.etsi.nfv.nsd.model.NsdCsar;
import org.openecomp.sdc.be.plugins.etsi.nfv.nsd.security.exception.NsdSignatureException;
import org.springframework.core.env.Environment;

class NsdCsarEtsiOption2SignerTest {

    private static final String CERT_NAME = "nsdCert";

    @Mock
    private CertificateManager certificateManager;
    @Mock
    private CmsContentSigner cmsContentSigner;
    @Mock
    private Environment environment;
    @Mock
    private CertificateInfo certificateInfo;
    @InjectMocks
    private NsdCsarEtsiOption2Signer nsdCsarEtsiOption2Signer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(environment.getProperty(SDC_NSD_CERT_NAME)).thenReturn(CERT_NAME);
        when(certificateManager.getCertificate(CERT_NAME)).thenReturn(Optional.of(certificateInfo));
        when(certificateInfo.isValid()).thenReturn(true);
    }

    @Test
    void signNsdTest() throws NsdSignatureException, CmsSignatureException {
        final NsdCsar nsdCsar = new NsdCsar("");
        nsdCsar.addFile("aFile", "aFile".getBytes(StandardCharsets.UTF_8));
        final byte[] aFileSigned = "aFileSigned".getBytes(StandardCharsets.UTF_8);
        when(cmsContentSigner.signData(eq("aFile".getBytes(StandardCharsets.UTF_8)), any(), any())).thenReturn(
            aFileSigned);
        final String aFileSignedPemString = "aFileSignedPemString";
        when(cmsContentSigner.formatToPemSignature(aFileSigned)).thenReturn(aFileSignedPemString);
        nsdCsarEtsiOption2Signer.signArtifacts(nsdCsar);
        assertThat("The NSD CSAR should contain the original file and its signature",
            nsdCsar.getFileMap().keySet(), hasSize(2));
        assertThat("The signed file should be as expected",
            nsdCsar.getFile("aFile" + SIGNATURE_EXTENSION), is(aFileSignedPemString.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void dontCreateNsdManifestSignatureFileTest() throws NsdSignatureException {
        final NsdCsar nsdCsar = new NsdCsar("nsdCsar");
        nsdCsar.addFile(nsdCsar.getManifestPath(), "manifest".getBytes(StandardCharsets.UTF_8));
        nsdCsarEtsiOption2Signer.signArtifacts(nsdCsar);
        assertThat("The NSD CSAR should contain only the original file",
            nsdCsar.getFileMap().keySet(), hasSize(1));
        assertThat("The NSD CSAR should not contain the manifest signature file",
            nsdCsar.getFile(nsdCsar.getManifestPath() + SIGNATURE_EXTENSION), is(nullValue()));
    }

    @Test
    void signEmptyNsdTest() throws NsdSignatureException {
        final NsdCsar nsdCsar = new NsdCsar("");
        nsdCsarEtsiOption2Signer.signArtifacts(nsdCsar);
        assertThat("The NSD CSAR should continue empty", nsdCsar.isEmpty(), is(true));
    }

    @Test
    void signNsdNoCertificateTest() {
        when(certificateManager.getCertificate(CERT_NAME)).thenReturn(Optional.empty());
        final NsdCsar nsdCsar = new NsdCsar("");
        nsdCsar.addFile("anyFile", "anyFile".getBytes());
        final NsdSignatureException actualException = assertThrows(NsdSignatureException.class,
            () -> nsdCsarEtsiOption2Signer.signArtifacts(nsdCsar));
        assertThat(actualException.getMessage(), is(certificateNotConfigured().getMessage()));
    }

    @Test
    void signWholeNoCertificateTest() {
        when(certificateManager.getCertificate(CERT_NAME)).thenReturn(Optional.empty());
        final NsdSignatureException actualException =
            Assertions.assertThrows(NsdSignatureException.class,
                () -> nsdCsarEtsiOption2Signer.sign(new byte[]{}));
        assertThat(actualException.getMessage(), is(certificateNotConfigured().getMessage()));
    }

    @Test
    void signWithInvalidCertificateTest() {
        when(certificateInfo.isValid()).thenReturn(false);
        final NsdSignatureException actualException =
            Assertions.assertThrows(NsdSignatureException.class,
                () -> nsdCsarEtsiOption2Signer.sign(new byte[]{}));
        assertThat(actualException.getMessage(), is(invalidCertificate(null).getMessage()));
    }

    @Test
    void signWholeFileTest() throws NsdSignatureException, CmsSignatureException {
        final byte[] nsdCsarBytes = "nsdCsarBytes".getBytes(StandardCharsets.UTF_8);
        final NsdCsar nsdCsar = new NsdCsar("");
        nsdCsar.addFile("aFile", "aFile".getBytes(StandardCharsets.UTF_8));
        final byte[] nsdCsarBytesSigned = "nsdCsarBytesSigned".getBytes(StandardCharsets.UTF_8);
        when(cmsContentSigner.signData(eq(nsdCsarBytes), any(), any())).thenReturn(nsdCsarBytesSigned);
        final String nsdCsarBytesSignedPemString = "nsdCsarBytesSignedPemString";
        when(cmsContentSigner.formatToPemSignature(nsdCsarBytesSigned)).thenReturn(nsdCsarBytesSignedPemString);
        final byte[] actualNsdSignedCsar = nsdCsarEtsiOption2Signer.sign(nsdCsarBytes);
        assertThat("Signature should be as expected",
            actualNsdSignedCsar, is(nsdCsarBytesSignedPemString.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void signatureCreationErrorTest() throws CmsSignatureException {
        final byte[] nsdCsarBytes = "nsdCsarBytes".getBytes(StandardCharsets.UTF_8);
        final NsdCsar nsdCsar = new NsdCsar("");
        nsdCsar.addFile("aFile", "aFile".getBytes(StandardCharsets.UTF_8));
        when(cmsContentSigner.signData(eq(nsdCsarBytes), any(), any()))
            .thenThrow(new CmsSignatureException(null, null));
        final NsdSignatureException actualException = assertThrows(NsdSignatureException.class,
            () -> nsdCsarEtsiOption2Signer.sign(nsdCsarBytes));
        assertThat(actualException.getMessage(), is(unableToCreateSignature(null).getMessage()));
    }

    @Test
    void getSigningCertificateTest() {
        when(certificateManager.getCertificate(CERT_NAME)).thenReturn(Optional.empty());
        Optional<CertificateInfo> signingCertificate = nsdCsarEtsiOption2Signer.getSigningCertificate();
        assertThat("Certificate should not be present", signingCertificate.isEmpty(), is(true));
        when(certificateManager.getCertificate(CERT_NAME)).thenReturn(Optional.of(certificateInfo));
        signingCertificate = nsdCsarEtsiOption2Signer.getSigningCertificate();
        assertThat("Certificate should be present", signingCertificate.isPresent(), is(true));
        assertThat("Certificate should be as expected", signingCertificate.get(), is(certificateInfo));
    }

    @Test
    void isCertificateConfiguredTest() {
        when(certificateManager.getCertificate(CERT_NAME)).thenReturn(Optional.empty());
        boolean isCertificateConfigured = nsdCsarEtsiOption2Signer.isCertificateConfigured();
        assertThat("Certificate should not be configured", isCertificateConfigured, is(false));
        when(certificateManager.getCertificate(CERT_NAME)).thenReturn(Optional.of(certificateInfo));
        isCertificateConfigured = nsdCsarEtsiOption2Signer.isCertificateConfigured();
        assertThat("Certificate should be configured", isCertificateConfigured, is(true));
    }
}