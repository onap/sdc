/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * Modifications copyright (c) 2021 Nokia
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

package org.openecomp.sdc.vendorsoftwareproduct.services.impl.filedatastructuremodule;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.openecomp.sdc.heat.datatypes.manifest.FileData;
import org.openecomp.sdc.heat.datatypes.manifest.ManifestContent;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.vendorsoftwareproduct.types.candidateheat.FilesDataStructure;

class ManifestCreatorNamingConventionImplTest extends ManifestCreatorNamingConventionImpl {

    private static final String ARTIFACT_1 = "cloudtech_k8s_charts.zip";
    private static final String ARTIFACT_2 = "cloudtech_azure_day0.zip";
    private static final String ARTIFACT_3 = "cloudtech_aws_configtemplate.zip";
    private static final String ARTIFACT_4 = "k8s_charts.zip";
    private static final String ARTIFACT_5 = "cloudtech_openstack_configtemplate.zip";
    private static final String PMDICT_YAML = "pmdict.yaml";

    @Test
    void testIsCloudSpecificArtifact() {
        assertTrue(isCloudSpecificArtifact(ARTIFACT_1, null));
        assertTrue(isCloudSpecificArtifact(ARTIFACT_2, null));
        assertTrue(isCloudSpecificArtifact(ARTIFACT_3, null));
        assertFalse(isCloudSpecificArtifact(ARTIFACT_4, null));
        assertFalse(isCloudSpecificArtifact(ARTIFACT_5, null));
    }

    @Test
    void shouldMapPmDictionaryTypeFromExistingManifestToPmDictionaryTypeInNewManifest() {
        // given
        VspDetails vspDetails = new VspDetails();
        FilesDataStructure fileDataStructure = new FilesDataStructure();
        fileDataStructure.setArtifacts(List.of(PMDICT_YAML));
        ManifestContent existingManifest = prepareManifestWithPmDictFileWithType(FileData.Type.PM_DICTIONARY);

        // when
        Optional<ManifestContent> newManifest = new ManifestCreatorNamingConventionImpl()
            .createManifestFromExisting(vspDetails, fileDataStructure, existingManifest);

        // then
        assertTrue(newManifest.isPresent());
        assertTrue(newManifest.get()
            .getData()
            .stream()
            .allMatch(fd -> fd.getType().equals(FileData.Type.PM_DICTIONARY) &&
                fd.getFile().equals(PMDICT_YAML)));
    }

    @Test
    void shouldMapPmDictionaryWithOtherTypeFromExistingManifestToOtherTypeInNewManifest() {
        // given
        VspDetails vspDetails = new VspDetails();
        FilesDataStructure fileDataStructure = new FilesDataStructure();
        fileDataStructure.setArtifacts(List.of(PMDICT_YAML));
        ManifestContent existingManifest = prepareManifestWithPmDictFileWithType(FileData.Type.OTHER);

        // when
        Optional<ManifestContent> newManifest = new ManifestCreatorNamingConventionImpl()
            .createManifestFromExisting(vspDetails, fileDataStructure, existingManifest);

        // then
        assertTrue(newManifest.isPresent());
        assertTrue(newManifest.get()
            .getData()
            .stream()
            .allMatch(fd -> fd.getType().equals(FileData.Type.OTHER) &&
                fd.getFile().equals(PMDICT_YAML)));
    }

    private ManifestContent prepareManifestWithPmDictFileWithType(FileData.Type fileType) {
        ManifestContent existingManifest = new ManifestContent();
        FileData fileData = new FileData();
        fileData.setFile(PMDICT_YAML);
        fileData.setType(fileType);
        existingManifest.setData(List.of(fileData));
        return existingManifest;
    }
}
