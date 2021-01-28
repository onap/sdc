/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nordix Foundation
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
package org.openecomp.sdc.be.plugins.etsi.nfv.nsd.builder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.openecomp.sdc.be.plugins.etsi.nfv.nsd.builder.NsdCsarManifestBuilder.ATTRIBUTE_SEPARATOR;
import static org.openecomp.sdc.be.plugins.etsi.nfv.nsd.builder.NsdCsarManifestBuilder.COMPATIBLE_SPECIFICATION_VERSIONS;
import static org.openecomp.sdc.be.plugins.etsi.nfv.nsd.builder.NsdCsarManifestBuilder.METADATA;
import static org.openecomp.sdc.be.plugins.etsi.nfv.nsd.builder.NsdCsarManifestBuilder.NSD_RELEASE_DATE_TIME;
import static org.openecomp.sdc.be.plugins.etsi.nfv.nsd.builder.NsdCsarManifestBuilder.SOURCE;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class NsdCsarManifestBuilderTest {

    @Test
    void testBuildSuccess() {
        final NsdCsarManifestBuilder nsdCsarManifestBuilder = new NsdCsarManifestBuilder();
        nsdCsarManifestBuilder.withDesigner("designer");
        nsdCsarManifestBuilder.withName("name");
        nsdCsarManifestBuilder.withInvariantId("invariantId");
        nsdCsarManifestBuilder.withFileStructureVersion("fileStructureVersion");
        nsdCsarManifestBuilder.withCompatibleSpecificationVersion("1.0.0");
        nsdCsarManifestBuilder.withCompatibleSpecificationVersion("1.0.1");
        final String signature = "-----BEGIN CMS-----\n"
            + "12d08j19d981928129dj129j1\n"
            + "-----END CMS-----";
        nsdCsarManifestBuilder.withSignature(signature);
        final List<String> sourceList = new ArrayList<>();
        final String source1 = "Definitions/aSource1.yaml";
        sourceList.add(source1);
        final String source2 = "Definitions/aSource2.yaml";
        sourceList.add(source2);
        nsdCsarManifestBuilder.withSources(sourceList);
        final String manifest = nsdCsarManifestBuilder.build();
        assertSource(manifest, source1);
        assertSource(manifest, source2);
        assertCompatibleSpecificationVersions(manifest, "1.0.0,1.0.1");
        final String expectedManifest = "metadata: \n"
            + "nsd_designer: designer\n"
            + "nsd_invariant_id: invariantId\n"
            + "nsd_name: name\n"
            + "nsd_file_structure_version: fileStructureVersion\n"
            + "compatible_specification_versions: 1.0.0,1.0.1\n"
            + "\n"
            + "Source: Definitions/aSource1.yaml\n"
            + "Source: Definitions/aSource2.yaml\n"
            + signature;
        assertEquals(expectedManifest, manifest);
    }

    @Test
    void testMetadataReleaseDateTime() {
        final NsdCsarManifestBuilder nsdCsarManifestBuilder = new NsdCsarManifestBuilder();
        nsdCsarManifestBuilder.withNowReleaseDateTime();
        final String manifest = nsdCsarManifestBuilder.build();
        System.out.println(manifest);
        assertTrue(manifest.contains(METADATA + ATTRIBUTE_SEPARATOR));
        assertTrue(manifest.contains(NSD_RELEASE_DATE_TIME + ATTRIBUTE_SEPARATOR));
    }

    @Test
    void testDuplicatedCompatibleSpecificationVersion() {
        final NsdCsarManifestBuilder nsdCsarManifestBuilder = new NsdCsarManifestBuilder();
        nsdCsarManifestBuilder.withCompatibleSpecificationVersion("1.0.0");
        nsdCsarManifestBuilder.withCompatibleSpecificationVersion("1.0.0");
        final String manifest = nsdCsarManifestBuilder.build();
        assertCompatibleSpecificationVersions(manifest, "1.0.0");
        assertFalse(manifest.contains(COMPATIBLE_SPECIFICATION_VERSIONS + ATTRIBUTE_SEPARATOR + "1.0.0,1.0.0"));
    }

    @Test
    void testCompatibleSpecificationVersionSuccess() {
        final NsdCsarManifestBuilder nsdCsarManifestBuilder = new NsdCsarManifestBuilder();
        nsdCsarManifestBuilder.withCompatibleSpecificationVersion("1.0.0");
        String manifest = nsdCsarManifestBuilder.build();
        assertCompatibleSpecificationVersions(manifest, "1.0.0");
        nsdCsarManifestBuilder.withCompatibleSpecificationVersion("2.0.0");
        nsdCsarManifestBuilder.withCompatibleSpecificationVersion("3.0.0");
        manifest = nsdCsarManifestBuilder.build();
        assertCompatibleSpecificationVersions(manifest, "1.0.0,2.0.0,3.0.0");
    }

    void assertCompatibleSpecificationVersions(final String manifest, final String versions) {
        assertTrue(manifest.contains(COMPATIBLE_SPECIFICATION_VERSIONS + ATTRIBUTE_SEPARATOR + versions));
    }

    void assertSource(final String manifest, final String source) {
        assertTrue(manifest.contains(SOURCE + ATTRIBUTE_SEPARATOR + source));
    }
}
