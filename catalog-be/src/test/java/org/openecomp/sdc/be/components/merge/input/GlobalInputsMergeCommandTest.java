package org.openecomp.sdc.be.components.merge.input;

import fj.data.Either;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.openecomp.sdc.be.auditing.impl.AuditingManager;
import org.openecomp.sdc.be.components.impl.utils.ExceptionUtils;
import org.openecomp.sdc.be.components.merge.GlobalInputsFilteringBusinessLogic;
import org.openecomp.sdc.be.components.utils.ObjectGenerator;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.InputDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.openecomp.sdc.be.components.utils.Conditions.hasPropertiesWithNames;
import static org.openecomp.sdc.be.dao.utils.CollectionUtils.union;

public class GlobalInputsMergeCommandTest extends BaseComponentInputsMerge {

    private GlobalInputsMergeCommand testInstance;
    @Mock
    private GlobalInputsFilteringBusinessLogic globalInputsFilteringBusinessLogic;
    @Mock
    private ExceptionUtils exceptionUtils;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        new ComponentInputsMergeBL(inputsValuesMergingBusinessLogic, declaredInputsResolver, toscaOperationFacade, new ComponentsUtils(mock(AuditingManager.class)));
        testInstance = new GlobalInputsMergeCommand(inputsValuesMergingBusinessLogic, declaredInputsResolver, toscaOperationFacade, new ComponentsUtils(mock(AuditingManager.class)), globalInputsFilteringBusinessLogic, exceptionUtils);
    }

    @Test
    public void mergeOnlyGlobalInputs_redeclareOnlyGroupAndPolicyProperties() {
        List<InputDefinition> globalInputs = ObjectGenerator.buildInputs("input1");
        List<InputDefinition> prevDeclaredInputs = ObjectGenerator.buildInputs("input2", "input3");
        List<InputDefinition> expectedInputsToMerge = new ArrayList<>(globalInputs);
        List<InputDefinition> expectedInputsToUpdate = union(globalInputs, prevDeclaredInputs);

        when(globalInputsFilteringBusinessLogic.filterGlobalInputs(currResource)).thenReturn(Either.left(globalInputs));
        when(declaredInputsResolver.getPreviouslyDeclaredInputsToMerge(eq(prevResource), eq(currResource), getInputPropertiesCaptor.capture())).thenReturn(prevDeclaredInputs);
        when(toscaOperationFacade.updateInputsToComponent(expectedInputsToUpdate, RESOURCE_ID)).thenReturn(Either.left(null));
        doCallRealMethod().when(inputsValuesMergingBusinessLogic).mergeComponentInputs(Mockito.anyList(), Mockito.anyList());
        ActionStatus actionStatus = testInstance.mergeComponents(prevResource, currResource);
        assertThat(actionStatus).isEqualTo(ActionStatus.OK);
        verifyCallToMergeComponentInputs(prevResource, expectedInputsToMerge);
        verifyGroupsAndPolicyPropertiesPassedToDeclaredInputsResolver();
    }

    private void verifyGroupsAndPolicyPropertiesPassedToDeclaredInputsResolver() {
        Map<String, List<PropertyDataDefinition>> allResourceProps = getInputPropertiesCaptor.getValue();
        assertThat(allResourceProps)
                .hasEntrySatisfying("group1", hasPropertiesWithNames("prop1"))
                .hasEntrySatisfying("policy1", hasPropertiesWithNames("prop2"));
    }
}