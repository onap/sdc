package org.openecomp.sdc.be.components.impl;

import com.google.common.collect.ImmutableMap;
import fj.data.Either;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.DummyConfigurationManager;
import org.openecomp.sdc.be.components.utils.PolicyTypeBuilder;
import org.openecomp.sdc.be.components.validation.UserValidations;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.jsongraph.TitanDao;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.PolicyTypeDefinition;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.PolicyTypeOperation;
import org.openecomp.sdc.exception.ResponseFormat;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PolicyTypeBusinessLogicTest {

    private static final String USER_ID = "userId";
    private static final Set<String> EXCLUDED_POLICY_TYPES = newHashSet("type1", "type2");
    private static final String COMPONENT_TYPE = "VF";

    @InjectMocks
    private PolicyTypeBusinessLogic testInstance;
    @Mock
    private TitanDao titanDao;
    @Mock
    private PolicyTypeOperation policyTypeOperation;
    @Mock
    private ComponentsUtils componentsUtils;
    @Mock
    private UserValidations userValidations;

    @Before
    public void setUp() throws Exception {
        when(userValidations.validateUserExists(eq(USER_ID), anyString(), eq(true))).thenReturn(Either.left(new User()));
        when(componentsUtils.convertToResponseFormatOrNotFoundErrorToEmptyList(any(StorageOperationStatus.class))).thenCallRealMethod();
        when(ConfigurationManager.getConfigurationManager().getConfiguration().getExcludedPolicyTypesMapping()).thenReturn(ImmutableMap.of(COMPONENT_TYPE, EXCLUDED_POLICY_TYPES));
    }

    @BeforeClass
    public static void beforeClass() {
        new DummyConfigurationManager();
    }

    @Test
    public void getAllPolicyTypes_userNotExist() {
        ResponseFormat userNotExistResponse = new ResponseFormat();
        when(userValidations.validateUserExists(eq(USER_ID), anyString(), eq(true))).thenReturn(Either.right(userNotExistResponse));
        Either<List<PolicyTypeDefinition>, ResponseFormat> allPolicyTypes = testInstance.getAllPolicyTypes(USER_ID, COMPONENT_TYPE);
        assertThat(allPolicyTypes.right().value()).isSameAs(userNotExistResponse);
    }

    @Test
    public void getAllPolicyTypes_whenExcludePolicyTypesSetIsNull_passNullExcludedTypesSet() {
        when(ConfigurationManager.getConfigurationManager().getConfiguration().getExcludedPolicyTypesMapping()).thenCallRealMethod();
        when(policyTypeOperation.getAllPolicyTypes(null)).thenReturn(Either.left(emptyList()));
        Either<List<PolicyTypeDefinition>, ResponseFormat> allPolicyTypes = testInstance.getAllPolicyTypes(USER_ID, COMPONENT_TYPE);
        assertThat(allPolicyTypes.left().value()).isEmpty();
    }

    @Test
    public void getAllPolicyTypes() {
        List<PolicyTypeDefinition> policyTypes = Arrays.asList(new PolicyTypeBuilder().setUniqueId("id1").build(),
                new PolicyTypeBuilder().setUniqueId("id2").build(),
                new PolicyTypeBuilder().setUniqueId("id3").build());
        when(policyTypeOperation.getAllPolicyTypes(EXCLUDED_POLICY_TYPES)).thenReturn(Either.left(policyTypes));
        Either<List<PolicyTypeDefinition>, ResponseFormat> allPolicyTypes = testInstance.getAllPolicyTypes(USER_ID, COMPONENT_TYPE);
        assertThat(allPolicyTypes.left().value()).isSameAs(policyTypes);
    }

    @Test
    public void getAllPolicyTypes_noPolicyTypes() {
        when(policyTypeOperation.getAllPolicyTypes(EXCLUDED_POLICY_TYPES)).thenReturn(Either.right(StorageOperationStatus.NOT_FOUND));
        Either<List<PolicyTypeDefinition>, ResponseFormat> allPolicyTypes = testInstance.getAllPolicyTypes(USER_ID, COMPONENT_TYPE);
        assertThat(allPolicyTypes.left().value()).isEmpty();
        verify(titanDao).commit();
    }

    @Test
    public void getAllPolicyTypes_err() {
        when(policyTypeOperation.getAllPolicyTypes(EXCLUDED_POLICY_TYPES)).thenReturn(Either.right(StorageOperationStatus.GENERAL_ERROR));
        ResponseFormat errResponse = new ResponseFormat();
        when(componentsUtils.getResponseFormat(StorageOperationStatus.GENERAL_ERROR)).thenReturn(errResponse);
        Either<List<PolicyTypeDefinition>, ResponseFormat> allPolicyTypes = testInstance.getAllPolicyTypes(USER_ID, COMPONENT_TYPE);
        assertThat(allPolicyTypes.right().value()).isSameAs(errResponse);
        verify(titanDao).commit();
    }
}