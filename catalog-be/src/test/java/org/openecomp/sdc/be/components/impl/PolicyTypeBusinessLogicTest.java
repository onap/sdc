package org.openecomp.sdc.be.components.impl;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.DummyConfigurationManager;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.components.utils.PolicyTypeBuilder;
import org.openecomp.sdc.be.components.validation.UserValidations;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.PolicyTypeDefinition;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.operations.StorageException;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.PolicyTypeOperation;
import org.openecomp.sdc.exception.ResponseFormat;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PolicyTypeBusinessLogicTest {

    private static final String USER_ID = "userId";
    private static final Set<String> EXCLUDED_POLICY_TYPES = newHashSet("type1", "type2");
    private static final String COMPONENT_TYPE = "VF";

    @InjectMocks
    private PolicyTypeBusinessLogic testInstance;
    @Mock
    private JanusGraphDao janusGraphDao;
    @Mock
    private PolicyTypeOperation policyTypeOperation;
    @Mock
    private ComponentsUtils componentsUtils;
    @Mock
    private UserValidations userValidations;

    @Before
    public void setUp() throws Exception {
        when(userValidations.validateUserExists(eq(USER_ID), anyString(), eq(true))).thenReturn(new User());
        when(ConfigurationManager.getConfigurationManager().getConfiguration().getExcludedPolicyTypesMapping()).thenReturn(ImmutableMap.of(COMPONENT_TYPE, EXCLUDED_POLICY_TYPES));
    }

    @BeforeClass
    public static void beforeClass() {
        new DummyConfigurationManager();
    }

    @Test
    public void getAllPolicyTypes_userNotExist() {
        ResponseFormat userNotExistResponse = new ResponseFormat();
        when(userValidations.validateUserExists(eq(USER_ID), anyString(), eq(true))).thenThrow(new ComponentException(userNotExistResponse));
        try{
            testInstance.getAllPolicyTypes(USER_ID, COMPONENT_TYPE);
        }catch(ComponentException e){
            assertThat(e.getResponseFormat()).isSameAs(userNotExistResponse);
        }
    }

    @Test
    public void getAllPolicyTypes_whenExcludePolicyTypesSetIsNull_passNullExcludedTypesSet() {
        when(ConfigurationManager.getConfigurationManager().getConfiguration().getExcludedPolicyTypesMapping()).thenCallRealMethod();
        when(policyTypeOperation.getAllPolicyTypes(anySet())).thenReturn(emptyList());
        List<PolicyTypeDefinition> allPolicyTypes = testInstance.getAllPolicyTypes(USER_ID, COMPONENT_TYPE);
        assertThat(allPolicyTypes).isEmpty();
    }

    @Test
    public void getAllPolicyTypes() {
        List<PolicyTypeDefinition> policyTypes = Arrays.asList(new PolicyTypeBuilder().setUniqueId("id1").build(),
                new PolicyTypeBuilder().setUniqueId("id2").build(),
                new PolicyTypeBuilder().setUniqueId("id3").build());
        when(policyTypeOperation.getAllPolicyTypes(EXCLUDED_POLICY_TYPES)).thenReturn(policyTypes);
        List<PolicyTypeDefinition> allPolicyTypes = testInstance.getAllPolicyTypes(USER_ID, COMPONENT_TYPE);
        assertThat(allPolicyTypes).isSameAs(policyTypes);
    }

    @Test
    public void getAllPolicyTypes_noPolicyTypes() {
        when(policyTypeOperation.getAllPolicyTypes(EXCLUDED_POLICY_TYPES)).thenThrow(new StorageException(StorageOperationStatus.NOT_FOUND));
        try {
            testInstance.getAllPolicyTypes(USER_ID, COMPONENT_TYPE);
        }catch(StorageException e){
            assertThat(e.getStorageOperationStatus()).isSameAs(StorageOperationStatus.NOT_FOUND);
        }
    }

}