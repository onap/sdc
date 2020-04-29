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

import static org.apache.commons.collections4.ListUtils.union;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.openecomp.sdc.be.components.utils.Conditions.hasPropertiesWithNames;

import fj.data.Either;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openecomp.sdc.be.auditing.impl.AuditingManager;
import org.openecomp.sdc.be.components.utils.ObjectGenerator;
import org.openecomp.sdc.be.components.utils.ResourceBuilder;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;

@ExtendWith(MockitoExtension.class)
public class ComponentInputsMergeBLTest extends BaseComponentInputsMerge {

    @InjectMocks
    private ComponentInputsMergeBL testInstance;

    @Mock
    private AuditingManager auditingManager;

    @Mock
    private ComponentsUtils componentsUtils;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    @DisplayName("When old component has no inputs then return ok")
    public void whenOldComponentHasNoInputs_returnOk() {
        ActionStatus actionStatus = testInstance.mergeComponents(new Resource(), new Resource());
        assertThat(actionStatus).isEqualTo(ActionStatus.OK);
        verifyNoInteractions(toscaOperationFacade, inputsValuesMergingBusinessLogic, declaredInputsResolver);
    }

    @Test
    @DisplayName("When current resource has no properties no redeclaration of inputs required")
    public void whenCurrResourceHasNoProperties_noRedeclarationOFInputsRequired() {
        Resource newResource = new ResourceBuilder().setUniqueId(RESOURCE_ID).build();
        when(toscaOperationFacade.updateInputsToComponent(Collections.emptyList(), RESOURCE_ID))
            .thenReturn(Either.left(null));
        doCallRealMethod().when(inputsValuesMergingBusinessLogic)
            .mergeComponentInputs(Mockito.anyList(), Mockito.anyList());
        ActionStatus actionStatus = testInstance.mergeComponents(prevResource, newResource);
        assertThat(actionStatus).isEqualTo(ActionStatus.OK);
        verifyCallToMergeComponentInputs(prevResource, Collections.emptyList());
    }

    @Test
    @DisplayName("When current resource has no inputs no merge required, update resource with inputs declared in previous version")
    public void whenCurrResourceHasNoInputs_noMergeRequired_updateResourceWithInputsDeclaredInPrevVersion() {
        List<InputDefinition> prevDeclaredInputs = ObjectGenerator.buildInputs("declared1", "declared2");
        currResource.setInputs(null);
        when(declaredInputsResolver
            .getPreviouslyDeclaredInputsToMerge(ArgumentMatchers.eq(prevResource), ArgumentMatchers.eq(currResource),
                getInputPropertiesCaptor.capture()))
            .thenReturn(prevDeclaredInputs);
        when(toscaOperationFacade.updateInputsToComponent(prevDeclaredInputs, RESOURCE_ID))
            .thenReturn(Either.left(null));
        doCallRealMethod().when(inputsValuesMergingBusinessLogic)
            .mergeComponentInputs(Mockito.anyList(), Mockito.anyList());
        ActionStatus actionStatus = testInstance.mergeComponents(prevResource, currResource);
        assertThat(actionStatus).isEqualTo(ActionStatus.OK);
        verifyCallToMergeComponentInputs(prevResource, Collections.emptyList());
        verifyPropertiesPassedToDeclaredInputsResolver();
    }

    @Test
    @DisplayName("Find inputs declared from properties and merge them into new component")
    public void findInputsDeclaredFromPropertiesAndMergeThemIntoNewComponent() {
        List<InputDefinition> prevDeclaredInputs = ObjectGenerator.buildInputs("declared1", "declared2");
        List<InputDefinition> currInputsPreMerge = new ArrayList<>(currResource.getInputs());
        when(declaredInputsResolver
            .getPreviouslyDeclaredInputsToMerge(ArgumentMatchers.eq(prevResource), ArgumentMatchers.eq(currResource),
                getInputPropertiesCaptor.capture()))
            .thenReturn(prevDeclaredInputs);
        List<InputDefinition> expectedInputsToUpdate = union(currInputsPreMerge, prevDeclaredInputs);
        when(toscaOperationFacade.updateInputsToComponent(expectedInputsToUpdate, RESOURCE_ID))
            .thenReturn(Either.left(null));
        doCallRealMethod().when(inputsValuesMergingBusinessLogic)
            .mergeComponentInputs(Mockito.anyList(), Mockito.anyList());
        ActionStatus actionStatus = testInstance.mergeComponents(prevResource, currResource);
        assertThat(actionStatus).isEqualTo(ActionStatus.OK);
        verifyCallToMergeComponentInputs(prevResource, currInputsPreMerge);
        verifyPropertiesPassedToDeclaredInputsResolver();
    }

    @Test
    @DisplayName("Identify already existing inputs and don't merge them into new component")
    public void identifyAlreadyExistingInputsAndDontMergeThemIntoNewComponent() {
        List<InputDefinition> prevDeclaredInputs = ObjectGenerator.buildInputs("declared1", "declared2", "input1");
        List<InputDefinition> prevDeclaredInputsNotPresentInCurrent = ObjectGenerator
            .buildInputs("declared1", "declared2");
        List<InputDefinition> currInputsPreMerge = new ArrayList<>(currResource.getInputs());
        when(declaredInputsResolver
            .getPreviouslyDeclaredInputsToMerge(ArgumentMatchers.eq(prevResource), ArgumentMatchers.eq(currResource),
                getInputPropertiesCaptor.capture()))
            .thenReturn(prevDeclaredInputs);
        List<InputDefinition> expectedInputsToUpdate = union(currInputsPreMerge, prevDeclaredInputsNotPresentInCurrent);
        when(toscaOperationFacade.updateInputsToComponent(expectedInputsToUpdate, RESOURCE_ID))
            .thenReturn(Either.left(null));
        doCallRealMethod().when(inputsValuesMergingBusinessLogic)
            .mergeComponentInputs(Mockito.anyList(), Mockito.anyList());
        ActionStatus actionStatus = testInstance.mergeComponents(prevResource, currResource);
        assertThat(actionStatus).isEqualTo(ActionStatus.OK);
        assertThat(currResource.getInputs()).containsExactlyInAnyOrderElementsOf(expectedInputsToUpdate);
        verifyCallToMergeComponentInputs(prevResource, currInputsPreMerge);
        verifyPropertiesPassedToDeclaredInputsResolver();
    }

    @Test
    @DisplayName("When failing to update inputs propagate the error")
    public void whenFailingToUpdateInputs_propagateTheError() {
        Resource newResource = new ResourceBuilder().setUniqueId(RESOURCE_ID).build();
        when(toscaOperationFacade.updateInputsToComponent(any(), any()))
            .thenReturn(Either.right(StorageOperationStatus.GENERAL_ERROR));
        when(componentsUtils.convertFromStorageResponse(StorageOperationStatus.GENERAL_ERROR))
            .thenReturn(ActionStatus.GENERAL_ERROR);
        ActionStatus actionStatus = testInstance.mergeComponents(prevResource, newResource);
        assertThat(actionStatus).isEqualTo(ActionStatus.GENERAL_ERROR);
    }

    private void verifyPropertiesPassedToDeclaredInputsResolver() {
        Map<String, List<PropertyDataDefinition>> allResourceProps = getInputPropertiesCaptor.getValue();
        assertThat(allResourceProps)
            .hasEntrySatisfying("inst1", hasPropertiesWithNames("prop1", "prop2"))
            .hasEntrySatisfying("inst2", hasPropertiesWithNames("prop3"))
            .hasEntrySatisfying("group1", hasPropertiesWithNames("prop1"))
            .hasEntrySatisfying("policy1", hasPropertiesWithNames("prop2"));
    }
}

