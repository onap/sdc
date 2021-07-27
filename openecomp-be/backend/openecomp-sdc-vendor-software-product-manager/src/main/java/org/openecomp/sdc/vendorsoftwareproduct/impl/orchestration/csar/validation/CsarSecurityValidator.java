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

import java.io.IOException;
import java.util.Optional;
import lombok.NoArgsConstructor;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.security.SecurityManager;
import org.openecomp.sdc.vendorsoftwareproduct.security.SecurityManagerException;
import org.openecomp.sdc.vendorsoftwareproduct.types.OnboardPackageInfo;
import org.openecomp.sdc.vendorsoftwareproduct.types.OnboardSignedPackage;

/**
 * Validates the package security
 */
@NoArgsConstructor
public class CsarSecurityValidator {

    private static final Logger logger = LoggerFactory.getLogger(CsarSecurityValidator.class);
    private SecurityManager securityManager = SecurityManager.getInstance();

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
    public boolean verifyPackageSignature(final OnboardPackageInfo onboardPackageInfo) throws SecurityManagerException {
        if (isArtifactStorageEnabled(onboardPackageInfo)) {
            try {
                return securityManager.verifyPackageSignedData(onboardPackageInfo);
            } catch (final IOException e) {
                logger.warn("Failed to verify package signature", e);
                return false;
            }
        } else {
            final OnboardSignedPackage signedPackage = (OnboardSignedPackage) onboardPackageInfo.getOriginalOnboardPackage();
            final var fileContentHandler = signedPackage.getFileContentHandler();
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

    private boolean isArtifactStorageEnabled(final OnboardPackageInfo onboardPackageInfo) {
        return onboardPackageInfo.getArtifactInfo() != null && onboardPackageInfo.getArtifactInfo().getPath() != null;
    }

}
