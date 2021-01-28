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

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.cert.Certificate;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import org.openecomp.sdc.be.csar.security.api.CertificateManager;
import org.openecomp.sdc.be.csar.security.api.CmsContentSigner;
import org.openecomp.sdc.be.csar.security.api.model.CertificateInfo;
import org.openecomp.sdc.be.csar.security.exception.CmsSignatureException;
import org.openecomp.sdc.be.plugins.etsi.nfv.nsd.builder.NsdCsarManifestBuilder;
import org.openecomp.sdc.be.plugins.etsi.nfv.nsd.model.NsdCsar;
import org.openecomp.sdc.be.plugins.etsi.nfv.nsd.security.exception.NsdSignatureException;
import org.openecomp.sdc.be.plugins.etsi.nfv.nsd.security.exception.NsdSignatureExceptionSupplier;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Handles NSD CSAR file and package signature, following the ETSI SOL007 v3.3.1, section 5.1, signing option 2.
 *
 * @see <a href="https://www.etsi.org/deliver/etsi_gs/NFV-SOL/001_099/007/03.03.01_60/gs_NFV-SOL007v030301p.pdf">ETSI SOL007 v3.3.1 documentation</a>
 */
@Component
public class NsdCsarEtsiOption2Signer {

    public static final String SDC_NSD_CERT_NAME = "SDC_NSD_CERT_NAME";
    public static final String SIGNATURE_EXTENSION = ".sig.cms";

    private final CertificateManager certificateManager;
    private final CmsContentSigner cmsContentSigner;
    private final Environment environment;

    public NsdCsarEtsiOption2Signer(final CertificateManager certificateManager,
                                    final CmsContentSigner cmsContentSigner,
                                    final Environment environment) {
        this.certificateManager = certificateManager;
        this.cmsContentSigner = cmsContentSigner;
        this.environment = environment;
    }

    /**
     * Sign each NSD CSAR artifact (files), generating a cms file for each. The manifest, though, have its signature added in its body instead of a separate
     * CMS file. Modifies the given NSD CSAR by adding the file signatures and the modified manifest.
     *
     * @param nsdCsar the NSD CSAR
     * @throws NsdSignatureException when there was a problem while creating a file signature
     */
    public void signArtifacts(final NsdCsar nsdCsar) throws NsdSignatureException {
        if (nsdCsar == null) {
            return;
        }
        //ignore the manifest, the signature of the manifest goes inside the manifest itself ETSI 3.3.1 section 5.3
        final Map<String, byte[]> fileMap = nsdCsar.getFileMap().entrySet().stream()
            .filter(entry -> !nsdCsar.isManifest(entry.getKey()))
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

        for (final Entry<String, byte[]> fileEntry : fileMap.entrySet()) {
            final byte[] signatureBytes = sign(fileEntry.getValue());
            final String filePath = fileEntry.getKey();
            nsdCsar.addFile(filePath + SIGNATURE_EXTENSION, signatureBytes);
        }

        signManifest(nsdCsar);
    }

    private void signManifest(final NsdCsar nsdCsar) throws NsdSignatureException {
        final Optional<Entry<String, byte[]>> manifestEntryOpt = nsdCsar.getFileMap().entrySet().stream()
            .filter(entry -> nsdCsar.isManifest(entry.getKey())).findFirst();
        if (manifestEntryOpt.isEmpty()) {
            return;
        }
        final CertificateInfo certificateInfo = getValidCertificate();

        final Entry<String, byte[]> manifestEntry = manifestEntryOpt.get();
        final String pemSignature = createFileSignature(certificateInfo.getCertificate(),
            certificateInfo.getPrivateKey(), manifestEntry.getValue());
        final NsdCsarManifestBuilder manifestBuilder = nsdCsar.getManifestBuilder();
        manifestBuilder.withSignature(pemSignature);
        nsdCsar.addManifest(manifestBuilder);
    }

    /**
     * Sign a file, creating the PEM format signature and returning its bytes.
     *
     * @param fileBytes the file to sign
     * @return the bytes of the PEM format signature created from the given file and the NSD certificate
     * @throws NsdSignatureException when it was not possible to retrieve the NSD certificate
     * @throws NsdSignatureException when the NSD certificate is invalid
     * @throws NsdSignatureException it was not possible to sign the file
     */
    public byte[] sign(final byte[] fileBytes) throws NsdSignatureException {
        final CertificateInfo certificateInfo = getValidCertificate();
        final String pemSignature =
            createFileSignature(certificateInfo.getCertificate(), certificateInfo.getPrivateKey(), fileBytes);
        return pemSignature.getBytes(StandardCharsets.UTF_8);
    }

    public Optional<CertificateInfo> getSigningCertificate() {
        final String sdcNsdCertName = environment.getProperty(SDC_NSD_CERT_NAME);
        return certificateManager.getCertificate(sdcNsdCertName);
    }

    public boolean isCertificateConfigured() {
        return getSigningCertificate().isPresent();
    }

    private CertificateInfo getValidCertificate() throws NsdSignatureException {
        final Optional<CertificateInfo> certificateInfoOpt = getSigningCertificate();
        if (certificateInfoOpt.isEmpty()) {
            throw NsdSignatureExceptionSupplier.certficateNotConfigured();
        }
        final CertificateInfo certificateInfo = certificateInfoOpt.get();
        if (!certificateInfo.isValid()) {
            throw NsdSignatureExceptionSupplier.invalidCertificate(certificateInfo.getName());
        }

        return certificateInfo;
    }

    private String createFileSignature(final Certificate certificate, final Key privateKey,
                                       final byte[] fileBytes) throws NsdSignatureException {
        try {
            final byte[] dataSignature = cmsContentSigner.signData(fileBytes, certificate, privateKey);
            return cmsContentSigner.formatToPemSignature(dataSignature);
        } catch (final CmsSignatureException e) {
            throw NsdSignatureExceptionSupplier.unableToCreateSignature(e);
        }
    }
}
