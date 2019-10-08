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
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

@RunWith(MockitoJUnitRunner.class)
public class SoftwareInformationBusinessLogicTest {

    @Mock
    private PropertyBusinessLogic propertyBusinessLogic;
    @Mock
    private CsarInfo csarInfo;

    private SoftwareInformationBusinessLogic softwareInformationBusinessLogic;

    @Before
    public void setup() throws IOException {
        softwareInformationBusinessLogic = new SoftwareInformationBusinessLogic(propertyBusinessLogic);
        mockCsarInfo();
    }

    private void mockCsarInfo() throws IOException {
        final String softwareInformationPath = "Artifact/Informational/SW_INFORMATION";
        final byte[] softwareInformationFile = TestUtils
            .getResourceAsByteArray("artifacts/pnfSoftwareInformation/pnf-sw-information.yaml");
        final HashMap<String, byte[]> csarFileMap = new HashMap<>();
        csarFileMap.put(softwareInformationPath, softwareInformationFile);
        when(csarInfo.getCsar()).thenReturn(csarFileMap);
        when(csarInfo.getSoftwareInformationPath()).thenReturn(Optional.of(softwareInformationPath));
    }

    @Test
    public void testRemoveSoftwareInformationFile() {
        final boolean result = softwareInformationBusinessLogic.removeSoftwareInformationFile(csarInfo);
        assertThat("The software information file should be removed", result, is(true));
    }

    @Test
    public void testSetSoftwareInformation() throws BusinessLogicException {
        final Resource resource = Mockito.mock(Resource.class);
        final List<PropertyDefinition> propertyDefinitionList = new ArrayList<>();
        final PropertyDefinition propertyDefinition = new PropertyDefinition();
        propertyDefinition.setName("software_versions");
        propertyDefinitionList.add(propertyDefinition);
        when(resource.getProperties()).thenReturn(propertyDefinitionList);

        when(propertyBusinessLogic.updateComponentProperty(Mockito.any(), Mockito.any())).thenReturn(propertyDefinition);
        final Optional<PropertyDefinition> actualPropertyDefinition = softwareInformationBusinessLogic
            .setSoftwareInformation(resource, csarInfo);
        assertThat("The updated property should be present", actualPropertyDefinition.isPresent(), is(true));
        actualPropertyDefinition.ifPresent(propertyDefinition1 -> {
            assertThat("The updated property should have the expected name", propertyDefinition1.getName(), is("software_versions"));
            assertThat("The updated property should have the expected value", propertyDefinition1.getValue(), is("[\"version1\",\"version2\"]"));
        });
    }

}