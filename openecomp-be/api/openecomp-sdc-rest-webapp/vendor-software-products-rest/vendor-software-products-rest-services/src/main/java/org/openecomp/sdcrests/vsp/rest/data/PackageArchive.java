/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019, Nordix Foundation. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.openecomp.sdcrests.vsp.rest.data;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.sdc.common.utils.CommonUtil;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.security.SecurityManager;
import org.openecomp.sdc.vendorsoftwareproduct.security.SecurityManagerException;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Class responsible for processing zip archive and verify if this package corresponds
 * SOL004 option 2 signed package format, verifies the cms signature if package is signed
 */
public class PackageArchive {
    private static final Logger LOG = LoggerFactory.getLogger(PackageArchive.class);
    private static final String[] ALLOWED_ARCHIVE_EXTENSIONS = {"csar", "zip"};
    private static final String[] ALLOWED_SIGNATURE_EXTENSIONS = {"cms"};
    private static final String[] ALLOWED_CERTIFICATE_EXTENSIONS = {"cert"};
    private static final int NUMBER_OF_FILES_FOR_SIGNATURE_WITH_CERT_INSIDE = 2;
    private static final int NUMBER_OF_FILES_FOR_SIGNATURE_WITHOUT_CERT_INSIDE = 3;
    private final SecurityManager securityManager;
    private final byte[] outerPackageFileBytes;
    private Pair<FileContentHandler, List<String>> handlerPair;

    public PackageArchive(Attachment uploadedFile) {
        this(uploadedFile.getObject(byte[].class));
    }

    public PackageArchive(byte[] outerPackageFileBytes) {
        this.outerPackageFileBytes = outerPackageFileBytes;
        this.securityManager = SecurityManager.getInstance();
        try {
            handlerPair = CommonUtil.getFileContentMapFromOrchestrationCandidateZip(
                    outerPackageFileBytes);
        } catch (IOException exception) {
            LOG.error("Error reading files inside archive", exception);
        }
    }

    /**
     * Checks if package matches required format {package.csar/zip, package.cms, package.cert(optional)}
     *
     * @return true if structure matches sol004 option 2 structure
     */
    public boolean isSigned() {
        return isPackageSizeMatches() && getSignatureFileName().isPresent();
    }

    /**
     * Gets csar/zip package name with extension only if package is signed
     *
     * @return csar package name
     */
    public Optional<String> getArchiveFileName() {
        if (isSigned()) {
            return getFileByExtension(ALLOWED_ARCHIVE_EXTENSIONS);
        }
        return Optional.empty();
    }

    /**
     * Gets csar/zip package content from zip archive
     * @return csar package content
     * @throws SecurityManagerException
     */
    public byte[] getPackageFileContents() throws SecurityManagerException {
        try {
            if (isSignatureValid()) {
                return handlerPair.getKey().getFiles().get(getArchiveFileName().orElseThrow(CertificateException::new));
            }
        } catch (CertificateException exception) {
            LOG.info("Error verifying signature " + exception);
        }
        return outerPackageFileBytes;
    }

    /**
     * Validates package signature against trusted certificates
     * @return true if signature verified
     * @throws SecurityManagerException
     */
    public boolean isSignatureValid() throws SecurityManagerException {
        Map<String, byte[]> files = handlerPair.getLeft().getFiles();
        Optional<String> signatureFileName = getSignatureFileName();
        Optional<String> archiveFileName = getArchiveFileName();
        if (files.isEmpty() || !signatureFileName.isPresent() || !archiveFileName.isPresent()) {
            return false;
        }
        Optional<String> certificateFile = getCertificateFileName();
        if(certificateFile.isPresent()){
            return securityManager.verifySignedData(files.get(signatureFileName.get()),
                    files.get(certificateFile.get()), files.get(archiveFileName.get()));
        }else {
            return securityManager.verifySignedData(files.get(signatureFileName.get()),
                    null, files.get(archiveFileName.get()));
        }
    }

    private boolean isPackageSizeMatches() {
        return handlerPair.getRight().isEmpty()
                && (handlerPair.getLeft().getFiles().size() == NUMBER_OF_FILES_FOR_SIGNATURE_WITH_CERT_INSIDE
                || handlerPair.getLeft().getFiles().size() == NUMBER_OF_FILES_FOR_SIGNATURE_WITHOUT_CERT_INSIDE);
    }

    private Optional<String> getSignatureFileName() {
        return getFileByExtension(ALLOWED_SIGNATURE_EXTENSIONS);
    }

    private Optional<String> getFileByExtension(String[] extensions) {
        for (String fileName : handlerPair.getLeft().getFileList()) {
            for (String extension : extensions) {
                if (extension.equalsIgnoreCase(FilenameUtils.getExtension(fileName))) {
                    return Optional.of(fileName);
                }
            }
        }
        return Optional.empty();
    }

    private Optional<String> getCertificateFileName() {
        Optional<String> certFileName = getFileByExtension(ALLOWED_CERTIFICATE_EXTENSIONS);
        if(!certFileName.isPresent()){
            return Optional.empty();
        }
        String certNameWithoutExtension = FilenameUtils.removeExtension(certFileName.get());
        if (certNameWithoutExtension.equals(FilenameUtils.removeExtension(getArchiveFileName().orElse("")))) {
            return certFileName;
        }
        //cert file name should be the same as package name, e.g. vnfpackage.scar-->vnfpackage.cert
        return Optional.empty();
    }
}
