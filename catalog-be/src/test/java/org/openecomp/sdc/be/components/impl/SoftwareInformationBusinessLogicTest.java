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

package org.openecomp.sdc.be.components.impl;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.TestUtils;
import org.openecomp.sdc.be.components.csar.CsarInfo;
import org.openecomp.sdc.be.components.impl.exceptions.BusinessLogicException;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(MockitoJUnitRunner.class)
public class SoftwareInformationBusinessLogicTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SoftwareInformationBusinessLogicTest.class);
    private final String softwareInformationPath = "Artifact/Informational/SW_INFORMATION";

    @Mock
    private PropertyBusinessLogic propertyBusinessLogic;
    @Mock
    private CsarInfo csarInfo;
    @Mock
    private Resource resource;

    private SoftwareInformationBusinessLogic softwareInformationBusinessLogic;

    @Before
    public void setup() {
        softwareInformationBusinessLogic = new SoftwareInformationBusinessLogic(propertyBusinessLogic);
        mockCsarInfo();
    }

    private void mockCsarInfo() {
        mockCsarFileMap("artifacts/pnfSoftwareInformation/pnf-sw-information.yaml");
        when(csarInfo.getSoftwareInformationPath()).thenReturn(Optional.of(softwareInformationPath));
    }

    @Test
    public void testRemoveSoftwareInformationFile() {
        boolean result = softwareInformationBusinessLogic.removeSoftwareInformationFile(csarInfo);
        assertThat("The software information file should be removed", result, is(true));
        when(csarInfo.getSoftwareInformationPath()).thenReturn(Optional.empty());
        result = softwareInformationBusinessLogic.removeSoftwareInformationFile(csarInfo);
        assertThat("The software information file should not be removed", result, is(false));
    }

    @Test
    public void testSetSoftwareInformation() throws BusinessLogicException {
        final PropertyDefinition propertyDefinition = mockSoftwareInformationPropertyDefinition();
        mockResource(propertyDefinition);
        when(propertyBusinessLogic.updateComponentProperty(Mockito.any(), Mockito.any()))
            .thenReturn(propertyDefinition);
        final Optional<PropertyDefinition> actualPropertyDefinition = softwareInformationBusinessLogic
            .setSoftwareInformation(resource, csarInfo);
        assertThat("The updated property should be present", actualPropertyDefinition.isPresent(), is(true));
        actualPropertyDefinition.ifPresent(propertyDefinition1 -> {
            assertThat("The updated property should have the expected name", propertyDefinition1.getName(),
                is("software_versions"));
            assertThat("The updated property should have the expected value", propertyDefinition1.getValue(),
                is("[\"version1\",\"version2\"]"));
        });
    }

    @Test
    public void testSetSoftwareInformationWithInvalidArtifact() throws BusinessLogicException {
        //given
        final PropertyDefinition propertyDefinition = mockSoftwareInformationPropertyDefinition();
        mockResource(propertyDefinition);
        mockCsarFileMap("artifacts/pnfSoftwareInformation/pnf-sw-information-corrupt.yaml");
        //when and then
        assertNotPresentPropertyDefinition();

        //given
        mockCsarFileMap("artifacts/pnfSoftwareInformation/invalid.yaml");
        //when and then
        assertNotPresentPropertyDefinition();

        //given
        mockCsarFileMap("artifacts/pnfSoftwareInformation/pnf-sw-information-invalid-1.yaml");
        //when and then
        assertNotPresentPropertyDefinition();

        //given
        mockCsarFileMap("artifacts/pnfSoftwareInformation/pnf-sw-information-invalid-2.yaml");
        //when and then
        assertNotPresentPropertyDefinition();

        //given
        mockCsarFileMap("artifacts/pnfSoftwareInformation/pnf-sw-information-invalid-3.yaml");
        //when and then
        assertNotPresentPropertyDefinition();
    }

    private void assertNotPresentPropertyDefinition() throws BusinessLogicException {
        final Optional<PropertyDefinition> actualPropertyDefinition =
            softwareInformationBusinessLogic.setSoftwareInformation(resource, csarInfo);
        assertThat("The updated property should not be present",
            actualPropertyDefinition.isPresent(), is(false));
    }

    @Test
    public void testSetSoftwareInformationWithNoResourceSoftwareInformationProperty() throws BusinessLogicException {
        //when and then
        assertNotPresentPropertyDefinition();
    }

    @Test
    public void testSetSoftwareInformationWithNoCsarSoftwareInformation() throws BusinessLogicException {
        //given
        when(csarInfo.getSoftwareInformationPath()).thenReturn(Optional.empty());
        //when and then
        assertNotPresentPropertyDefinition();
    }

    private void mockCsarFileMap(final String softwareInformationArtifactPath) {
        final byte[] softwareInformationFile;
        try {
            softwareInformationFile = TestUtils.getResourceAsByteArray(softwareInformationArtifactPath);
        } catch (final IOException e) {
            final String errorMsg = "Could not find software information artifact " + softwareInformationArtifactPath;
            LOGGER.error(errorMsg, e);
            fail(errorMsg);
            return;
        }
        final HashMap<String, byte[]> csarFileMap = new HashMap<>();
        csarFileMap.put(softwareInformationPath, softwareInformationFile);
        when(csarInfo.getCsar()).thenReturn(csarFileMap);
    }

    private PropertyDefinition mockSoftwareInformationPropertyDefinition() {
        final PropertyDefinition propertyDefinition = new PropertyDefinition();
        propertyDefinition.setName("software_versions");
        return propertyDefinition;
    }

    private void mockResource(final PropertyDefinition... propertyDefinition) {
        when(resource.getProperties()).thenReturn(Arrays.asList(propertyDefinition));
    }

}