/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.be.components.merge.input;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openecomp.sdc.be.components.utils.ResourceBuilder;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public abstract class BaseComponentInputsMerge {
    private final ConfigurationManager configurationManager = new ConfigurationManager(new FSConfigurationSource(ExternalConfiguration.getChangeListener(), "src/test/resources/config/catalog-be"));

    @Mock
    protected ToscaOperationFacade toscaOperationFacade;

    @Mock
    InputsValuesMergingBusinessLogic inputsValuesMergingBusinessLogic;

    @Mock
    DeclaredInputsResolver declaredInputsResolver;

    @Captor
    ArgumentCaptor<Map<String, List<PropertyDataDefinition>>> getInputPropertiesCaptor;

    Resource prevResource, currResource;
    protected static final String RESOURCE_ID = "newResourceId";

    public void setUp() throws Exception {
        prevResource = new ResourceBuilder()
                .addInput("input1")
                .addInput("input2")
                .addComponentInstance("inst1")
                .build();

        currResource = new ResourceBuilder()
                .addComponentInstance("inst1")
                .addInstanceProperty("inst1", "prop1")
                .addInstanceProperty("inst1", "prop2")
                .addInstanceInput("inst2", "prop3")
                .addGroupProperty("group1", "prop1")
                .addPolicyProperty("policy1", "prop2")
                .addInput("input1")
                .setUniqueId(RESOURCE_ID)
                .build();
    }

    void verifyCallToMergeComponentInputs(Resource oldResource, List<InputDefinition> inputsToMerge) {
        Map<String, InputDefinition> oldInputsByName = oldResource.getInputs().stream().collect(Collectors.toMap(InputDefinition::getName, Function.identity()));
        Map<String, InputDefinition> inputsToMergeByName = inputsToMerge.stream().collect(Collectors.toMap(InputDefinition::getName, Function.identity()));
        verify(inputsValuesMergingBusinessLogic).mergeComponentInputs(oldInputsByName, inputsToMergeByName);
    }
}
