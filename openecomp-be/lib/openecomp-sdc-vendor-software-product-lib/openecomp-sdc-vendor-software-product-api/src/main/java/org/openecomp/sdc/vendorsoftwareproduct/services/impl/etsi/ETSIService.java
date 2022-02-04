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

import com.vdurmont.semver4j.Semver;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.tosca.csar.Manifest;
import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;

public interface ETSIService {

    /**
     * Checks package structure is CSAR with TOSCA-Metadata directory according to SOL004 v2.5.1 and contains mandatory Entries in Tosca.meta
     *
     * @param handler contains csar artifacts
     * @return true if all condition matched, false otherwise
     * @throws IOException when TOSCA.meta file is invalid
     */
    boolean hasEtsiSol261Metadata(FileContentHandler handler) throws IOException;

    /**
     * Checks if the package is a ETSI package.
     *
     * @param fileContentHandler the CSAR file handler
     * @return {@code true} if the package is a ETSI package, {@code false} otherwise.
     * @throws IOException when it was not able to parse the TOSCA.meta file
     */
    boolean isEtsiPackage(final FileContentHandler fileContentHandler) throws IOException;

    /**
     * Checks if the package is ASD package.
     *
     * @param fileContentHandler the CSAR file handler
     * @return {@code true} if the package is an ASD package, {@code false} otherwise.
     * @throws IOException when it was not able to parse the TOSCA.meta file
     */
    boolean isAsdPackage(final FileContentHandler fileContentHandler) throws IOException;

    /**
     * Update file structure. Moves non mano files to the correct folder based on the manifest non mano type.
     *
     * @param handler The file handler containing the artifacts to move.
     * @return A Map with pairs of from and to path of the moved artifacts.
     */
    Optional<Map<String, Path>> moveNonManoFileToArtifactFolder(final FileContentHandler handler) throws IOException;

    /**
     * Updates the main descriptor paths referring the artifacts that were moved.
     *
     * @param toscaServiceModel      The tosca service model containing the main descriptor.
     * @param fromToMovedArtifactMap A Map representing the from and to artifacts path changes.
     */
    void updateMainDescriptorPaths(final ToscaServiceModel toscaServiceModel, final Map<String, Path> fromToMovedArtifactMap);

    /**
     * Retrieves the manifest file from the CSAR
     *
     * @param handler contains csar artifacts
     * @throws IOException when TOSCA.meta file or manifest file is invalid
     */
    Manifest getManifest(FileContentHandler handler) throws IOException;

    /**
     * Determines the type of resource that the CSAR represents
     *
     * @param handler contains csar artifacts
     * @throws IOException when TOSCA.meta file or manifest file is invalid
     */
    ResourceTypeEnum getResourceType(FileContentHandler handler) throws IOException;

    /**
     * Determines the type of resource that the CSAR represents
     *
     * @param manifest contains manifest content
     * @throws IOException when TOSCA.meta file or manifest file is invalid
     */
    ResourceTypeEnum getResourceType(Manifest manifest) throws IOException;

    Path getOriginalManifestPath(final FileContentHandler handler) throws IOException;

    /**
     * Determines the highest compatible specification version based on ETSI manifest file
     *
     * @param handler contains csar artifacts
     * @return Semver representing highest compatible specification version
     */
    Semver getHighestCompatibleSpecificationVersion(final FileContentHandler handler);

    /**
     * Verifies if SOL004 3.3.1 manifest file has onap_cnf_helm non mano entry
     *
     * @return true if manifest files has onap_cnf_helm non mano entry
     */
    boolean hasCnfEnhancements(final FileContentHandler fileContentHandler) throws IOException;

}
