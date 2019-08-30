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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyString;
import static org.openecomp.sdc.tosca.csar.ManifestTokenType.PNFD_ARCHIVE_VERSION;
import static org.openecomp.sdc.tosca.csar.ManifestTokenType.PNFD_NAME;
import static org.openecomp.sdc.tosca.csar.ManifestTokenType.PNFD_PROVIDER;
import static org.openecomp.sdc.tosca.csar.ManifestTokenType.PNFD_RELEASE_DATE_TIME;
import static org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.csar.validation.NonManoArtifactType.ONAP_PM_DICTIONARY;
import static org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.csar.validation.NonManoArtifactType.ONAP_VES_EVENTS;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.junit.Before;
import org.junit.Test;
import org.openecomp.sdc.tosca.csar.Manifest;
import org.openecomp.sdc.tosca.csar.SOL004ManifestOnboarding;

public class ManifestBuilderTest {

    private ManifestBuilder manifestBuilder;

    @Before
    public void setUp() {
        manifestBuilder = new ManifestBuilder();
    }

    @Test
    public void givenNoManifestInformation_whenBuildingManifest_thenEmptyStringShouldBeReturned() {
        final String manifest = manifestBuilder.build();
        assertThat("Manifest should be empty", manifest, isEmptyString());
    }

    @Test
    public void givenSourceFiles_whenBuildingManifestWithSources_thenManifestSourceListShouldBeTheSame() {
        final List<String> expectedSourceList = mockSourceList();
        mockManifestMetadata();
        expectedSourceList.forEach(source -> manifestBuilder.withSource(source));

        final Manifest onboardingManifest = parseManifest();
        final List<String> actualSourceList = onboardingManifest.getSources();

        assertThat("Source list should have the same size as expected source items", actualSourceList,
            hasSize(expectedSourceList.size()));
        assertThat("Source list should contain all expected source items", actualSourceList,
            hasItems(expectedSourceList.toArray(new String[0])));
        assertThat("Source list should contain only expected sources items", expectedSourceList,
            hasItems(actualSourceList.toArray(new String[expectedSourceList.size()])));
    }

    @Test
    public void givenSourceFiles_whenBuildingManifestWithSignedSources_thenManifestSourceListShouldBeTheSame() {
        final List<String> expectedSourceList = mockSourceList();
        mockManifestMetadata();
        expectedSourceList.forEach(sourceArtifact ->
            manifestBuilder.withSignedSource(sourceArtifact, "anyAlgorithm", "anyHash")
        );

        final Manifest onboardingManifest = parseManifest();
        final List<String> sources = onboardingManifest.getSources();

        assertThat("Source list should have the same size as expected source items", sources,
            hasSize(expectedSourceList.size()));
        assertThat("Source list should contain all expected source items", sources,
            hasItems(expectedSourceList.toArray(new String[0])));
        assertThat("Source list should contain only expected sources items", expectedSourceList,
            hasItems(sources.toArray(new String[expectedSourceList.size()])));
    }

    @Test
    public void givenMetadata_whenBuildingManifestWithMetadata_thenParsedManifestMetadataShouldBeTheSame() {
        final Map<String, String> expectedMetadataMap = new TreeMap<>();
        expectedMetadataMap.put(PNFD_NAME.getToken(), "myPnf");
        expectedMetadataMap.put(PNFD_PROVIDER.getToken(), "Acme");
        expectedMetadataMap.put(PNFD_ARCHIVE_VERSION.getToken(), "1.0");
        expectedMetadataMap.put(PNFD_RELEASE_DATE_TIME.getToken(), "2019-03-11T11:25:00+00:00");

        expectedMetadataMap.forEach((key, value) -> manifestBuilder.withMetaData(key, value));

        final Manifest onboardingManifest = parseManifest();
        final Map<String, String> actualMetadataMap = onboardingManifest.getMetadata();

        assertThat("Metadata should be as expected", actualMetadataMap, is(expectedMetadataMap));
    }

    @Test
    public void givenNonManoArtifacts_whenBuildingManifestWithArtifacts_thenParsedManifestNonManoArtifactsShouldBeTheSame() {
        mockManifestMetadata();
        mockManifestSource();

        final Map<String, List<String>> expectedNonManoArtifactMap = new TreeMap<>();
        expectedNonManoArtifactMap.put(ONAP_VES_EVENTS.getType(), Arrays.asList("Files/Events/MyPnf_Pnf_v1.yaml"));
        expectedNonManoArtifactMap.put(ONAP_PM_DICTIONARY.getType(), Arrays.asList("Files/Measurements/PM_Dictionary.yaml"));
        expectedNonManoArtifactMap.put("onap_yang_modules",
            Arrays.asList("Files/Yang_module/mynetconf.yang", "Files/Yang_module/mynetconf2.yang"));
        expectedNonManoArtifactMap
            .put("onap_others", Arrays.asList("Files/Guides/user_guide.txt", "Files/Test/test.txt"));

        expectedNonManoArtifactMap.forEach((key, artifacts) ->
            artifacts.forEach(s -> manifestBuilder.withNonManoArtifact(key, s))
        );

        final Manifest onboardingManifest = parseManifest();
        final Map<String, List<String>> actualNonManoArtifactMap = onboardingManifest.getNonManoSources();

        assertThat("Non Mano Sources should be as expected", actualNonManoArtifactMap, is(expectedNonManoArtifactMap));
    }

    private Manifest parseManifest() {
        final Manifest onboardingManifest = new SOL004ManifestOnboarding();
        onboardingManifest.parse(new ByteArrayInputStream(manifestBuilder.build().getBytes()));
        return onboardingManifest;
    }

    private List<String> mockSourceList() {
        return Arrays.asList("pnf_main_descriptor.mf"
            , "Definitions/pnf_main_descriptor.yaml"
            , "Definitions/etsi_nfv_sol001_pnfd_2_5_1_types.yaml"
            , "Definitions/etsi_nfv_sol001_vnfd_2_5_1_types.yaml"
            , "Files/ChangeLog.txt"
            , "Files/Events/MyPnf_Pnf_v1.yaml"
            , "Files/Guides/user_guide.txt"
            , "Files/Measurements/PM_Dictionary.yaml"
            , "Files/Scripts/my_script.sh"
            , "Files/Yang_module/mynetconf.yang"
            , "TOSCA-Metadata/TOSCA.meta"
        );
    }

    private void mockManifestMetadata() {
        manifestBuilder.withMetaData(PNFD_PROVIDER.getToken(), "provider");
        manifestBuilder.withMetaData(PNFD_NAME.getToken(), "name");
        manifestBuilder.withMetaData(PNFD_RELEASE_DATE_TIME.getToken(), "datetime");
        manifestBuilder.withMetaData(PNFD_ARCHIVE_VERSION.getToken(), "1.0");
    }

    private void mockManifestSource() {
        manifestBuilder.withSource("/test.yaml");
    }
}