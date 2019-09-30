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

package org.openecomp.sdc.vendorsoftwareproduct.impl.onboarding;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.apache.commons.io.FilenameUtils;
import org.openecomp.core.utilities.file.FileContentHandler;

public class OnboardingPackageContentHandler extends FileContentHandler {

    public OnboardingPackageContentHandler() {
    }

    public OnboardingPackageContentHandler(final FileContentHandler other) {
        super(other);
    }

    public Map<String, String> getFileAndSignaturePathMap(final Set<String> signatureExtensionSet) {
        final Map<String, byte[]> files = getFiles();
        final Map<String, String> signedFilePairMap = new HashMap<>();
        files.keySet().stream()
            .filter(filePath -> !signatureExtensionSet.contains(FilenameUtils.getExtension(filePath)))
            .forEach(filePath -> {
                final String filePathWithoutExtension = FilenameUtils.removeExtension(filePath);
                signatureExtensionSet.stream()
                    .map(extension -> String.format("%s.%s", filePathWithoutExtension, extension))
                    .filter(files::containsKey)
                    .forEach(file -> signedFilePairMap.put(filePath, file));
                signedFilePairMap.putIfAbsent(filePath, null);
            });
        return signedFilePairMap;
    }

}
