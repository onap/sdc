/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.be.components.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import fj.data.Either;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.InterfaceDefinition;
import org.openecomp.sdc.be.model.Model;
import org.openecomp.sdc.be.model.operations.api.IInterfaceLifecycleOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.ModelOperation;
import org.openecomp.sdc.be.utils.TypeUtils.ToscaTagNamesEnum;
import org.openecomp.sdc.exception.ResponseFormat;

public class InterfaceLifecycleTypeImportManagerTest {

    @InjectMocks
    private InterfaceLifecycleTypeImportManager importManager = new InterfaceLifecycleTypeImportManager();
    public static final CommonImportManager commonImportManager = Mockito.mock(CommonImportManager.class);
    public static final IInterfaceLifecycleOperation interfaceLifecycleOperation = Mockito.mock(IInterfaceLifecycleOperation.class);
    public static final ComponentsUtils componentsUtils = Mockito.mock(ComponentsUtils.class);
    public static final ModelOperation modelOperation = Mockito.mock(ModelOperation.class);

    @BeforeClass
    public static void beforeClass() throws IOException {
        when(interfaceLifecycleOperation.createInterfaceType(Mockito.any(InterfaceDefinition.class))).thenAnswer(
            new Answer<Either<InterfaceDefinition, StorageOperationStatus>>() {
                public Either<InterfaceDefinition, StorageOperationStatus> answer(InvocationOnMock invocation) {
                    Object[] args = invocation.getArguments();
                    return Either.left((InterfaceDefinition) args[0]);
                }

            });
        when(commonImportManager.createElementTypesFromYml(Mockito.anyString(), Mockito.any())).thenCallRealMethod();
        when(commonImportManager.createElementTypesFromToscaJsonMap(Mockito.any(), Mockito.any())).thenCallRealMethod();
    }

    @Before
    public void initMocks() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void createLifecycleTypesTest() throws IOException {
        final String ymlContent = getYmlContent();
        when(modelOperation.findModelByName("test")).thenReturn(Optional.of(new Model("test")));
        final Either<List<InterfaceDefinition>, ResponseFormat> createCapabilityTypes =
            importManager.createLifecycleTypes(ymlContent, "test", false);
        assertTrue(createCapabilityTypes.isLeft());
        final List<InterfaceDefinition> interfaceDefinitionList = createCapabilityTypes.left().value();
        assertThat("Interface definitions should not be empty", interfaceDefinitionList, is(not(empty())));
        final int expectedSize = 2;
        assertThat(String.format("Interface definitions should have the size %s", expectedSize),
            interfaceDefinitionList, hasSize(expectedSize));
        final String standardInterfaceType = "tosca.interfaces.node.lifecycle.Standard";
        final String nslcmInterfaceType = "tosca.interfaces.nfv.Nslcm";
        final Optional<InterfaceDefinition> standardInterfaceOpt = interfaceDefinitionList.stream().filter(
                interfaceDefinition -> standardInterfaceType.equals(interfaceDefinition.getType()))
            .findFirst();
        final Optional<InterfaceDefinition> nslcmInterfaceOpt = interfaceDefinitionList.stream().filter(
                interfaceDefinition -> nslcmInterfaceType.equals(interfaceDefinition.getType()))
            .findFirst();
        assertThat("", standardInterfaceOpt.isPresent(), is(true));
        assertThat("", nslcmInterfaceOpt.isPresent(), is(true));
        final InterfaceDefinition standardInterface = standardInterfaceOpt.get();
        final Set<String> expectedStandardInterfaceOperationSet = Stream
            .of("create", "configure", "start", "stop", "delete").collect(Collectors.toSet());
        assertThat(String.format("%s derived_from should be as expected", standardInterfaceType),
            standardInterface.getDerivedFrom(), is("tosca.interfaces.Root"));
        assertThat(String.format("%s operations should have the expected size", standardInterfaceType),
            standardInterface.getOperationsMap().keySet(), hasSize(expectedStandardInterfaceOperationSet.size()));
        assertThat(String.format("%s should contains the expected operations", standardInterfaceType),
            standardInterface.getOperationsMap().keySet(),
            containsInAnyOrder(expectedStandardInterfaceOperationSet.toArray()));

        final InterfaceDefinition nslcmInterface = nslcmInterfaceOpt.get();
        assertThat(String.format("%s derived_from should be as expected", nslcmInterfaceType),
            nslcmInterface.getDerivedFrom(), is("tosca.interfaces.Root"));
        assertThat(String.format("%s description should be as expected", nslcmInterfaceType),
            nslcmInterface.getDescription(),
            is("This interface encompasses a set of TOSCA "
                + "operations corresponding to NS LCM operations defined in ETSI GS NFV-IFA 013. as well as to preamble "
                + "and postamble procedures to the execution of the NS LCM operations."));
        final Set<String> expectedNsclmInterfaceOperationSet = Stream
            .of("instantiate_start", "instantiate", "instantiate_end", "terminate_start", "terminate",
                "terminate_end", "update_start", "update", "update_end", "scale_start", "scale", "scale_end",
                "heal_start", "heal", "heal_end").collect(Collectors.toSet());
        assertThat(String.format("%s operations should have the expected size", nslcmInterfaceType),
            nslcmInterface.getOperationsMap().keySet(),
            hasSize(expectedNsclmInterfaceOperationSet.size()));
        assertThat(String.format("%s should contains the expected operations", nslcmInterfaceType),
            nslcmInterface.getOperationsMap().keySet(),
            containsInAnyOrder(expectedNsclmInterfaceOperationSet.toArray()));
    }

    @Test
    public void createInterfaceDefinitionFromJson() {
        String interfaceType = "com.ericsson.so.interfaces.node.lifecycle.Detach";
        Map<String, Object> interfaceDefMap = new HashMap<>();
        interfaceDefMap.put(ToscaTagNamesEnum.DESCRIPTION.getElementName(), "description");
        interfaceDefMap.put(ToscaTagNamesEnum.DERIVED_FROM.getElementName(), "tosca.interfaces.Root");
        InterfaceDefinition interfaceDefinition = importManager.createInterfaceDefinition(interfaceType, interfaceDefMap);
        assertNotNull(interfaceDefinition);
        assertEquals("description", interfaceDefinition.getDescription());
        assertEquals("tosca.interfaces.Root", interfaceDefinition.getDerivedFrom());
    }


    private String getYmlContent() throws IOException {
        Path filePath = Paths.get("src/test/resources/types/interfaceLifecycleTypes.yml");
        byte[] fileContent = Files.readAllBytes(filePath);
        return new String(fileContent);
    }
}
