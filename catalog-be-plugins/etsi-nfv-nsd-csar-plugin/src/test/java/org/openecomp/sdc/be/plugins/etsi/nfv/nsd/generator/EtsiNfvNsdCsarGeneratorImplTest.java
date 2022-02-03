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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.openecomp.sdc.be.plugins.etsi.nfv.nsd.generator.config.CategoriesToGenerateNsd;
import static org.openecomp.sdc.common.api.ArtifactTypeEnum.ONBOARDED_PACKAGE;

import fj.data.Either;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.csar.security.model.CertificateInfoImpl;
import org.openecomp.sdc.be.dao.cassandra.ArtifactCassandraDao;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.plugins.etsi.nfv.nsd.exception.NsdException;
import org.openecomp.sdc.be.plugins.etsi.nfv.nsd.exception.VnfDescriptorException;
import org.openecomp.sdc.be.plugins.etsi.nfv.nsd.factory.NsDescriptorGeneratorFactory;
import org.openecomp.sdc.be.plugins.etsi.nfv.nsd.generator.config.EtsiVersion;
import org.openecomp.sdc.be.plugins.etsi.nfv.nsd.generator.config.NsDescriptorConfig;
import org.openecomp.sdc.be.plugins.etsi.nfv.nsd.model.Nsd;
import org.openecomp.sdc.be.plugins.etsi.nfv.nsd.model.NsdCsar;
import org.openecomp.sdc.be.plugins.etsi.nfv.nsd.model.VnfDescriptor;
import org.openecomp.sdc.be.plugins.etsi.nfv.nsd.security.NsdCsarEtsiOption2Signer;
import org.openecomp.sdc.be.plugins.etsi.nfv.nsd.security.exception.NsdSignatureException;
import org.openecomp.sdc.be.resources.data.DAOArtifactData;

class EtsiNfvNsdCsarGeneratorImplTest {

    private static final String SERVICE_NORMALIZED_NAME = "normalizedName";
    @Mock
    private VnfDescriptorGenerator vnfDescriptorGenerator;
    @Mock
    private NsDescriptorGenerator nsDescriptorGeneratorImpl;
    @Mock
    private NsDescriptorGeneratorFactory nsDescriptorGeneratorFactory;
    @Mock
    private ArtifactCassandraDao artifactCassandraDao;
    @Mock
    private NsdCsarEtsiOption2Signer nsdCsarEtsiOption2Signer;
    @Mock
    private Service service;
    private EtsiNfvNsdCsarGeneratorImpl etsiNfvNsdCsarGenerator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        final EtsiVersion version2_5_1 = EtsiVersion.VERSION_2_5_1;
        etsiNfvNsdCsarGenerator = new EtsiNfvNsdCsarGeneratorImpl(new NsDescriptorConfig(version2_5_1),
            vnfDescriptorGenerator, nsDescriptorGeneratorFactory, artifactCassandraDao, nsdCsarEtsiOption2Signer);
        when(nsDescriptorGeneratorFactory.create()).thenReturn(nsDescriptorGeneratorImpl);
    }

    @Test
    void generateNsdCsarSuccessfulTest() throws VnfDescriptorException, NsdException {
        mockServiceComponent();
        mockServiceComponentArtifacts();
        final NsdCsar nsdCsar = etsiNfvNsdCsarGenerator.generateNsdCsar(service);
        assertThat("The NSD CSAR should not be null", nsdCsar, is(notNullValue()));
        assertThat("The NSD CSAR should not be signed", nsdCsar.isSigned(), is(false));
        assertThat("The NSD CSAR content should not be null", nsdCsar.getCsarPackage(), is(notNullValue()));
    }

    @Test
    void generateSignedNsdCsarSuccessfulTest() throws VnfDescriptorException, NsdException, NsdSignatureException {
        mockServiceComponent();
        mockServiceComponentArtifacts();
        when(nsdCsarEtsiOption2Signer.isCertificateConfigured()).thenReturn(true);
        final String path = getClass().getClassLoader().getResource("aFile.txt").getPath();
        System.out.println(path);
        final CertificateInfoImpl certificateInfo = new CertificateInfoImpl(new File(path), null);
        when(nsdCsarEtsiOption2Signer.getSigningCertificate()).thenReturn(Optional.of(certificateInfo));
        when(nsdCsarEtsiOption2Signer.sign(any(byte[].class))).thenReturn("signedCsar".getBytes(StandardCharsets.UTF_8));
        final NsdCsar nsdCsar = etsiNfvNsdCsarGenerator.generateNsdCsar(service);
        verify(nsdCsarEtsiOption2Signer).signArtifacts(any(NsdCsar.class));
        assertThat("The NSD CSAR should not be null", nsdCsar, is(notNullValue()));
        assertThat("The NSD CSAR should be signed", nsdCsar.isSigned(), is(true));
        assertThat("The NSD CSAR content should not be null", nsdCsar.getCsarPackage(), is(notNullValue()));
        assertThat("The NSD CSAR name should be as expected", nsdCsar.getFileName(), is(SERVICE_NORMALIZED_NAME));
        assertThat("The NSD CSAR name should be as expected", nsdCsar.isEmpty(), is(false));
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
        final ArtifactDefinition instanceArtifact1 = mockComponentInstanceArtifact(componentInstance1, "instanceArtifact1");
        final VnfDescriptor vnfDescriptor1 = new VnfDescriptor();
        final List<VnfDescriptor> vnfDescriptorList = Collections.singletonList(vnfDescriptor1);
        final Nsd nsd = new Nsd();
        when(vnfDescriptorGenerator.generate(componentInstance1Name, instanceArtifact1)).thenReturn(Optional.of(vnfDescriptor1));
        when(nsDescriptorGeneratorImpl.generate(service, vnfDescriptorList)).thenReturn(Optional.of(nsd));

        final List<CategoryDefinition> categoryDefinitionList = new ArrayList<>();
        final CategoryDefinition nsComponentCategoryDefinition = new CategoryDefinition();
        nsComponentCategoryDefinition.setName(CategoriesToGenerateNsd.ETSI_NS_COMPONENT_CATEGORY.getCategoryName());
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

    private ArtifactDefinition mockComponentInstanceArtifact(final ComponentInstance componentInstance, final String instanceArtifactId) {
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
