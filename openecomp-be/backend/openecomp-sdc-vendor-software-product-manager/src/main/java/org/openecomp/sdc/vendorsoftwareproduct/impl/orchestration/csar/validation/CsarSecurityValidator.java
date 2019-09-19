/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation
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

package org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.csar.validation;

import java.util.Optional;
import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.sdc.vendorsoftwareproduct.security.SecurityManager;
import org.openecomp.sdc.vendorsoftwareproduct.security.SecurityManagerException;
import org.openecomp.sdc.vendorsoftwareproduct.types.OnboardSignedPackage;

/**
 * Validates the package security
 */
public class CsarSecurityValidator {

    private SecurityManager securityManager = SecurityManager.getInstance();

    public CsarSecurityValidator() {
    }

    //for tests purpose
    CsarSecurityValidator(final SecurityManager securityManager) {
        this.securityManager = securityManager;
    }

    /**
     * Validates package signature against trusted certificates
     *
     * @return true if signature verified
     * @throws SecurityManagerException when a certificate error occurs.
     */
    public boolean verifyPackageSignature(final OnboardSignedPackage signedPackage) throws SecurityManagerException {
        final FileContentHandler fileContentHandler = signedPackage.getFileContentHandler();
        final byte[] signatureBytes = fileContentHandler.getFileContent(signedPackage.getSignatureFilePath());
        final byte[] archiveBytes = fileContentHandler.getFileContent(signedPackage.getInternalPackageFilePath());
        byte[] certificateBytes = null;
        final Optional<String> certificateFilePath = signedPackage.getCertificateFilePath();
        if (certificateFilePath.isPresent()) {
            certificateBytes = fileContentHandler.getFileContent(certificateFilePath.get());
        }

        return securityManager.verifySignedData(signatureBytes, certificateBytes, archiveBytes);
    }
}
