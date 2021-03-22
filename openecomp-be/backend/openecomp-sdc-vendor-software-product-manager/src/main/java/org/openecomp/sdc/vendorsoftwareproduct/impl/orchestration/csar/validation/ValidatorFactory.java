/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */
package org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.csar.validation;

import static org.openecomp.sdc.tosca.csar.CSARConstants.ETSI_VERSION_2_7_1;

import java.io.IOException;
import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.sdc.vendorsoftwareproduct.services.impl.etsi.ETSIService;
import org.openecomp.sdc.vendorsoftwareproduct.services.impl.etsi.ETSIServiceImpl;

public class ValidatorFactory {

    private ValidatorFactory() {
    }

    /**
     * Returns a validator based on the contents of the csar package.
     *
     * @param fileContentHandler the csar package
     * @return Validator based on the contents of the csar package provided
     * @throws IOException when metafile is invalid
     */
    public static Validator getValidator(final FileContentHandler fileContentHandler) throws IOException {
        final ETSIService etsiService = new ETSIServiceImpl(null);
        if (!etsiService.isSol004WithToscaMetaDirectory(fileContentHandler)) {
            return new ONAPCsarValidator();
        }
        if (!etsiService.getHighestCompatibleSpecificationVersion(fileContentHandler).isLowerThan(ETSI_VERSION_2_7_1)) {
            if (etsiService.hasCnfEnhancements(fileContentHandler)) {
                return new SOL004Version4MetaDirectoryValidator();
            }
            return new SOL004Version3MetaDirectoryValidator();
        }
        return new SOL004MetaDirectoryValidator();
    }
}
