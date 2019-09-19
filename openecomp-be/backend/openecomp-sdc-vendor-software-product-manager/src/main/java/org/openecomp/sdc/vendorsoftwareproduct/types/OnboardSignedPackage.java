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

package org.openecomp.sdc.vendorsoftwareproduct.types;

import java.nio.ByteBuffer;
import java.util.Optional;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.openecomp.core.utilities.file.FileContentHandler;

@Getter
public class OnboardSignedPackage extends OnboardPackage {
    private String signatureFilePath;
    private String internalPackageFilePath;
    private String certificateFilePath;

    public OnboardSignedPackage(final String filename, final String fileExtension, final ByteBuffer fileContent,
                                final FileContentHandler fileContentHandler, final String signatureFilePath,
                                final String internalPackageFilePath, final String certificateFilePath) {
        super(filename, fileExtension, fileContent, fileContentHandler);
        this.signatureFilePath = signatureFilePath;
        this.internalPackageFilePath = internalPackageFilePath;
        this.certificateFilePath = certificateFilePath;
    }

    public Optional<String> getCertificateFilePath() {
        if (StringUtils.isEmpty(certificateFilePath)) {
            return Optional.empty();
        }
        return Optional.of(certificateFilePath);
    }
}
