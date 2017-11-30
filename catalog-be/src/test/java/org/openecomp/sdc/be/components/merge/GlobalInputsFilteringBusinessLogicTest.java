package org.openecomp.sdc.be.components.merge;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.components.impl.generic.GenericTypeBusinessLogic;
import org.openecomp.sdc.be.components.utils.ObjectGenerator;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;

import com.google.common.collect.Sets;

import fj.data.Either;

public class GlobalInputsFilteringBusinessLogicTest {

    private static final String GENERIC_TOSCA_TYPE = "myGenericType";

    @InjectMocks
    private GlobalInputsFilteringBusinessLogic testInstance;

    @Mock
    private GenericTypeBusinessLogic genericTypeBusinessLogicMock;

    @Mock
    private ToscaOperationFacade toscaOperationFacadeMock;

    @Mock
    private ComponentsUtils componentsUtils;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testFilterGlobalInputs() throws Exception {
        Resource mockResource = Mockito.mock(Resource.class);
        String myGenericType = GENERIC_TOSCA_TYPE;
        String[] genericProperties = {"property1", "property2"};
        String[] allInputs = {"property1", "property2", "property3", "property4"};
        when(mockResource.fetchGenericTypeToscaNameFromConfig()).thenReturn(myGenericType);
        when(mockResource.getInputs()).thenReturn(ObjectGenerator.buildInputs(allInputs));
        Resource genericNodeType = ObjectGenerator.buildResourceWithProperties(genericProperties);
        when(toscaOperationFacadeMock.getLatestCertifiedNodeTypeByToscaResourceName(myGenericType)).thenReturn(Either.left(genericNodeType));
        when(genericTypeBusinessLogicMock.generateInputsFromGenericTypeProperties(genericNodeType)).thenReturn(ObjectGenerator.buildInputs(genericProperties));
        Either<List<InputDefinition>, ActionStatus> globalInputsEither = testInstance.filterGlobalInputs(mockResource);
        verifyFilteredOnlyGlobalInputs(globalInputsEither, genericProperties);
    }

    @Test
    public void testFilterGlobalInputs_errorGettingGenericType_convertToActionStatusAndReturn() throws Exception {
        Resource mockResource = Mockito.mock(Resource.class);
        when(mockResource.fetchGenericTypeToscaNameFromConfig()).thenReturn(GENERIC_TOSCA_TYPE);
        when(toscaOperationFacadeMock.getLatestCertifiedNodeTypeByToscaResourceName(GENERIC_TOSCA_TYPE)).thenReturn(Either.right(StorageOperationStatus.GENERAL_ERROR));
        when(componentsUtils.convertFromStorageResponse(StorageOperationStatus.GENERAL_ERROR)).thenReturn(ActionStatus.GENERAL_ERROR);
        Either<List<InputDefinition>, ActionStatus> globalInputsEither = testInstance.filterGlobalInputs(mockResource);
        assertTrue(globalInputsEither.isRight());
        assertEquals(ActionStatus.GENERAL_ERROR, globalInputsEither.right().value());
        verifyZeroInteractions(genericTypeBusinessLogicMock);
    }

    private void verifyFilteredOnlyGlobalInputs(Either<List<InputDefinition>, ActionStatus> globalInputsEither, String[] genericProperties) {
        assertTrue(globalInputsEither.isLeft());
        List<InputDefinition> globalInputs = globalInputsEither.left().value();
        assertEquals(2, globalInputs.size());
        Set<String> actualGlobalInputNames = globalInputs.stream().map(InputDefinition::getName).collect(Collectors.toSet());
        assertEquals(Sets.newHashSet(genericProperties), actualGlobalInputNames);
    }
}