/*
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2023 Nordix Foundation. All rights reserved.
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
package org.openecomp.sdc.be.plugins;

import fj.data.Either;
import java.io.IOException;
import java.util.zip.ZipOutputStream;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.exception.ResponseFormat;

/**
 * Implementations of this interface shall generate a csar file.
 */
public interface CsarZipGenerator {

    /**
     * Generate the csar file.
     *
     * @param component the component the csar is based on
     * @return Map of name to contents for entries to be included in the csar
     */
    Either<ZipOutputStream, ResponseFormat> generateCsarZip(
        final Component component,
        boolean getFromCS,
        ZipOutputStream zip,
        boolean isInCertificationRequest,
        String createdBy,
        String fileName,
        boolean isAsdPackage
    ) throws IOException;

    String getModel();
}
