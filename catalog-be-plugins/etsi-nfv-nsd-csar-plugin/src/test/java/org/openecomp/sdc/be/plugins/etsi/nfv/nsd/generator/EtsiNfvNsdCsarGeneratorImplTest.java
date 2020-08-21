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
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.openecomp.sdc.be.plugins.etsi.nfv.nsd.generator.EtsiNfvNsCsarEntryGenerator.ETSI_NS_COMPONENT_CATEGORY;
import static org.openecomp.sdc.common.api.ArtifactTypeEnum.ONBOARDED_PACKAGE;

import fj.data.Either;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.dao.cassandra.ArtifactCassandraDao;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.plugins.etsi.nfv.nsd.exception.NsdException;
import org.openecomp.sdc.be.plugins.etsi.nfv.nsd.exception.VnfDescriptorException;
import org.openecomp.sdc.be.plugins.etsi.nfv.nsd.model.Nsd;
import org.openecomp.sdc.be.plugins.etsi.nfv.nsd.model.VnfDescriptor;
import org.openecomp.sdc.be.resources.data.DAOArtifactData;

class EtsiNfvNsdCsarGeneratorImplTest {

    @Mock
    private VnfDescriptorGenerator vnfDescriptorGenerator;
    @Mock
    private NsDescriptorGenerator nsDescriptorGeneratorImpl;
    @Mock
    private ArtifactCassandraDao artifactCassandraDao;
    @InjectMocks
    private EtsiNfvNsdCsarGeneratorImpl etsiNfvNsdCsarGenerator;
    @Mock
    private Service service;

    private static final String SERVICE_NORMALIZED_NAME = "normalizedName";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void generateNsdCsarSuccessfulTest() throws VnfDescriptorException, NsdException {
        mockServiceComponent();
        mockServiceComponentArtifacts();
        final byte[] nsdCsar = etsiNfvNsdCsarGenerator.generateNsdCsar(service);
        assertThat("", nsdCsar, is(notNullValue()));
    }

    @Test()
    void invalidComponentTest() {
        assertThrows(NsdException.class, () -> etsiNfvNsdCsarGenerator.generateNsdCsar(null));
    }

    private void mockServiceComponent() throws VnfDescriptorException, NsdException {
        when(service.getNormalizedName()).thenReturn(SERVICE_NORMALIZED_NAME);
        when(service.getComponentType()).thenReturn(ComponentTypeEnum.SERVICE);
        final String componentInstance1Name = "componentInstance1";
        final ComponentInstance componentInstance1 = mockServiceComponentInstance(componentInstance1Name);
        final ArtifactDefinition instanceArtifact1 = mockComponentInstanceArtifact(componentInstance1,
            "instanceArtifact1");

        final VnfDescriptor vnfDescriptor1 = new VnfDescriptor();
        final List<VnfDescriptor> vnfDescriptorList = Collections.singletonList(vnfDescriptor1);
        final Nsd nsd = new Nsd();
        when(vnfDescriptorGenerator.generate(componentInstance1Name, instanceArtifact1))
            .thenReturn(Optional.of(vnfDescriptor1));
        when(nsDescriptorGeneratorImpl.generate(service, vnfDescriptorList)).thenReturn(Optional.of(nsd));

        final List<CategoryDefinition> categoryDefinitionList = new ArrayList<>();
        final CategoryDefinition nsComponentCategoryDefinition = new CategoryDefinition();
        nsComponentCategoryDefinition.setName(ETSI_NS_COMPONENT_CATEGORY);
        categoryDefinitionList.add(nsComponentCategoryDefinition);
        when(service.getCategories()).thenReturn(categoryDefinitionList);
    }

    private void mockServiceComponentArtifacts() {
        final Map<String, ArtifactDefinition> allArtifactsMap = new HashMap<>();
        final String artifact1Id = "artifact1";
        final ArtifactDefinition artifact1 = mockArtifactDefinition(artifact1Id);
        final byte[] artifact1Bytes = new byte[1];
        allArtifactsMap.put(artifact1Id, artifact1);
        when(service.getAllArtifacts()).thenReturn(allArtifactsMap);
        final DAOArtifactData artifact1Data = new DAOArtifactData();
        artifact1Data.setDataAsArray(artifact1Bytes);
        when(artifactCassandraDao.getArtifact(artifact1Id)).thenReturn(Either.left(artifact1Data));
    }

    private ComponentInstance mockServiceComponentInstance(final String componentInstanceName) {
        final Map<String, ArtifactDefinition> deploymentArtifactMap = new HashMap<>();
        final String instanceArtifact1Id = "instanceArtifact1";
        final ArtifactDefinition instanceArtifact1 = mockArtifactDefinition(instanceArtifact1Id);
        instanceArtifact1.setToscaPresentationValue(JsonPresentationFields.ARTIFACT_TYPE, ONBOARDED_PACKAGE.getType());
        deploymentArtifactMap.put(instanceArtifact1Id, instanceArtifact1);
        DAOArtifactData instanceArtifact1Data = new DAOArtifactData();
        final byte[] instanceArtifact1Bytes = new byte[1];
        instanceArtifact1Data.setDataAsArray(instanceArtifact1Bytes);
        when(artifactCassandraDao.getArtifact(instanceArtifact1Id)).thenReturn(Either.left(instanceArtifact1Data));
        final ComponentInstance componentInstance = mock(ComponentInstance.class);
        when(componentInstance.getDeploymentArtifacts()).thenReturn(deploymentArtifactMap);
        when(componentInstance.getName()).thenReturn(componentInstanceName);
        final List<ComponentInstance> componentInstanceList = new ArrayList<>();
        componentInstanceList.add(componentInstance);
        when(service.getComponentInstances()).thenReturn(componentInstanceList);

        return componentInstance;
    }

    private ArtifactDefinition mockComponentInstanceArtifact(final ComponentInstance componentInstance,
                                                             final String instanceArtifactId) {
        final Map<String, ArtifactDefinition> deploymentArtifactMap = new HashMap<>();
        when(componentInstance.getDeploymentArtifacts()).thenReturn(deploymentArtifactMap);

        final ArtifactDefinition instanceArtifact1 = mockArtifactDefinition(instanceArtifactId);
        instanceArtifact1.setToscaPresentationValue(JsonPresentationFields.ARTIFACT_TYPE, ONBOARDED_PACKAGE.getType());
        deploymentArtifactMap.put(instanceArtifactId, instanceArtifact1);
        DAOArtifactData instanceArtifact1Data = new DAOArtifactData();
        final byte[] instanceArtifact1Bytes = new byte[1];
        instanceArtifact1Data.setDataAsArray(instanceArtifact1Bytes);
        when(artifactCassandraDao.getArtifact(instanceArtifactId)).thenReturn(Either.left(instanceArtifact1Data));
        return instanceArtifact1;
    }

    private ArtifactDefinition mockArtifactDefinition(final String artifactId) {
        final ArtifactDefinition artifact = new ArtifactDefinition();
        artifact.setEsId(artifactId);

        return artifact;
    }
}