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
import lombok.Getter;
import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.sdc.vendorsoftwareproduct.impl.onboarding.OnboardingPackageContentHandler;
import org.openecomp.sdc.common.exception.ZipException;
import org.openecomp.sdc.common.utils.CommonUtil;
import org.openecomp.sdc.vendorsoftwareproduct.exception.OnboardPackageException;

@Getter
public class OnboardPackage {

    private final String filename;
    private final String fileExtension;
    private final ByteBuffer fileContent;
    private final FileContentHandler fileContentHandler;

    public OnboardPackage(final String filename, final String fileExtension, final ByteBuffer fileContent,
                          final FileContentHandler fileContentHandler) {
        this.filename = filename;
        this.fileExtension = fileExtension;
        this.fileContent = fileContent;
        this.fileContentHandler = fileContentHandler;
    }

    public OnboardPackage(final String filename, final String fileExtension, final ByteBuffer fileContent)
        throws OnboardPackageException {
        this.filename = filename;
        this.fileExtension = fileExtension;
        this.fileContent = fileContent;
        try {
            fileContentHandler = new OnboardingPackageContentHandler(CommonUtil.getZipContent(fileContent.array()));
        } catch (final ZipException e) {
            throw new OnboardPackageException("Could not read the package content", e);
        }
    }

    public OnboardPackage(final String packageName, final String packageExtension, final byte[] packageContentBytes,
                          final FileContentHandler fileContentHandler) {
        this(packageName, packageExtension, ByteBuffer.wrap(packageContentBytes), fileContentHandler);
    }
}
