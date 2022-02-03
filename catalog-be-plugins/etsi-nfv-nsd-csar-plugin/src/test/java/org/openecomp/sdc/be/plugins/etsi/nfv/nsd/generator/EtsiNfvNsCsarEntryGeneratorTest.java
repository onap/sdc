/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Nordix Foundation
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
package org.openecomp.sdc.be.plugins.etsi.nfv.nsd.generator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;
import org.openecomp.sdc.be.plugins.etsi.nfv.nsd.generator.config.CategoriesToGenerateNsd;
import static org.openecomp.sdc.be.plugins.etsi.nfv.nsd.generator.EtsiNfvNsCsarEntryGenerator.ETSI_VERSION_METADATA;
import static org.openecomp.sdc.be.plugins.etsi.nfv.nsd.generator.EtsiNfvNsCsarEntryGenerator.NSD_FILE_PATH_FORMAT;
import static org.openecomp.sdc.be.plugins.etsi.nfv.nsd.generator.EtsiNfvNsCsarEntryGenerator.UNSIGNED_CSAR_EXTENSION;
import static org.openecomp.sdc.common.api.ArtifactTypeEnum.ETSI_PACKAGE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.plugins.etsi.nfv.nsd.exception.NsdException;
import org.openecomp.sdc.be.plugins.etsi.nfv.nsd.factory.EtsiNfvNsdCsarGeneratorFactory;
import org.openecomp.sdc.be.plugins.etsi.nfv.nsd.generator.config.EtsiVersion;
import org.openecomp.sdc.be.plugins.etsi.nfv.nsd.model.NsdCsar;

class EtsiNfvNsCsarEntryGeneratorTest {

    private static final String SERVICE_NORMALIZED_NAME = "normalizedName";
    private static final String CSAR_ENTRY_EMPTY_ASSERT = "Csar Entries should be empty";
    private static final EtsiVersion nsdVersion = EtsiVersion.VERSION_2_5_1;
    @Mock
    private EtsiNfvNsdCsarGeneratorFactory etsiNfvNsdCsarGeneratorFactory;
    @Mock
    private EtsiNfvNsdCsarGenerator etsiNfvNsdCsarGenerator;
    @Mock
    private Service service;
    @InjectMocks
    private EtsiNfvNsCsarEntryGenerator etsiNfvNsCsarEntryGenerator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(etsiNfvNsdCsarGeneratorFactory.create(nsdVersion)).thenReturn(etsiNfvNsdCsarGenerator);
    }

    @Test
    void successfullyEntryGenerationTest() throws NsdException {
        mockServiceComponent();
        final NsdCsar nsdCsar = new NsdCsar(SERVICE_NORMALIZED_NAME);
        nsdCsar.setCsarPackage(new byte[5]);
        when(etsiNfvNsdCsarGenerator.generateNsdCsar(service)).thenReturn(nsdCsar);
        final Map<String, byte[]> entryMap = etsiNfvNsCsarEntryGenerator.generateCsarEntries(service);
        assertThat("Csar Entries should contain only one entry", entryMap.size(), is(1));
        assertThat("Csar Entries should contain the expected entry", entryMap,
            hasEntry(String.format(NSD_FILE_PATH_FORMAT, ETSI_PACKAGE, SERVICE_NORMALIZED_NAME, UNSIGNED_CSAR_EXTENSION),
                nsdCsar.getCsarPackage()));
    }

    @Test
    void knownNsdGenerationErrorTest() throws NsdException {
        mockServiceComponent();
        when(etsiNfvNsdCsarGenerator.generateNsdCsar(service)).thenThrow(new NsdException(""));
        final Map<String, byte[]> entryMap = etsiNfvNsCsarEntryGenerator.generateCsarEntries(service);
        assertThat(CSAR_ENTRY_EMPTY_ASSERT, entryMap, is(anEmptyMap()));
    }

    @Test
    void unknownNsdGenerationErrorTest() throws NsdException {
        mockServiceComponent();
        when(etsiNfvNsdCsarGenerator.generateNsdCsar(service)).thenThrow(new RuntimeException());
        final Map<String, byte[]> entryMap = etsiNfvNsCsarEntryGenerator.generateCsarEntries(service);
        assertThat(CSAR_ENTRY_EMPTY_ASSERT, entryMap, is(anEmptyMap()));
    }

    @Test
    void componentNullOrNotAServiceTest() {
        Map<String, byte[]> entryMap = etsiNfvNsCsarEntryGenerator.generateCsarEntries(service);
        assertThat(CSAR_ENTRY_EMPTY_ASSERT, entryMap, is(anEmptyMap()));
        entryMap = etsiNfvNsCsarEntryGenerator.generateCsarEntries(null);
        assertThat(CSAR_ENTRY_EMPTY_ASSERT, entryMap, is(anEmptyMap()));
    }

    @Test
    void componentNotExpectedCategoryTest() {
        when(service.getComponentType()).thenReturn(ComponentTypeEnum.SERVICE);
        final List<CategoryDefinition> categoryDefinitionList = new ArrayList<>();
        final CategoryDefinition nsComponentCategoryDefinition = new CategoryDefinition();
        nsComponentCategoryDefinition.setName("notExpectedCategory");
        categoryDefinitionList.add(nsComponentCategoryDefinition);
        when(service.getCategories()).thenReturn(categoryDefinitionList);
        final Map<String, byte[]> entryMap = etsiNfvNsCsarEntryGenerator.generateCsarEntries(service);
        assertThat(CSAR_ENTRY_EMPTY_ASSERT, entryMap, is(anEmptyMap()));
    }

    private void mockServiceComponent() {
        when(service.getName()).thenReturn("anyName");
        when(service.getComponentType()).thenReturn(ComponentTypeEnum.SERVICE);
        when(service.getNormalizedName()).thenReturn(SERVICE_NORMALIZED_NAME);
        final Map<String, String> categorySpecificMetadataMap = new HashMap<>();
        categorySpecificMetadataMap.put(ETSI_VERSION_METADATA, nsdVersion.getVersion());
        when(service.getCategorySpecificMetadata()).thenReturn(categorySpecificMetadataMap);
        final List<CategoryDefinition> categoryDefinitionList = new ArrayList<>();
        final CategoryDefinition nsComponentCategoryDefinition = new CategoryDefinition();
        nsComponentCategoryDefinition.setName(CategoriesToGenerateNsd.ETSI_NS_COMPONENT_CATEGORY.getCategoryName());
        categoryDefinitionList.add(nsComponentCategoryDefinition);
        when(service.getCategories()).thenReturn(categoryDefinitionList);
    }
}
