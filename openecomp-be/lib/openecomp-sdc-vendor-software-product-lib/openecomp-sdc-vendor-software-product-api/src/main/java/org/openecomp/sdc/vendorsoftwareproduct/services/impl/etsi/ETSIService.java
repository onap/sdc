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

package org.openecomp.sdc.vendorsoftwareproduct.services.impl.etsi;

import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.sdc.tosca.csar.Manifest;
import java.io.IOException;


public interface ETSIService {

    /**
     * Checks package structure is CSAR with TOSCA-Metadata directory according to SOL004 v2.5.1
     * and contains mandatory Entries in Tosca.meta
     * @param handler contains csar artifacts
     * @return true if all condition matched, false otherwise
     * @throws IOException when TOSCA.meta file is invalid
     */
    boolean isSol004WithToscaMetaDirectory(FileContentHandler handler) throws IOException;

    /**
     * Update file structure. Moves non mano files to Artifacts/Deployment/non mano key location
     * @param handler
     * @param manifest
     */
    void moveNonManoFileToArtifactFolder(FileContentHandler handler, Manifest manifest);
}
