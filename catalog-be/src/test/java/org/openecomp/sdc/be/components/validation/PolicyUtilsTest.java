/*
 * Copyright © 2016-2019 European Support Limited
 *
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
 */


package org.openecomp.sdc.be.components.validation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import fj.data.Either;
import java.util.Objects;
import mockit.Deencapsulation;
import org.junit.Test;
import org.openecomp.sdc.be.components.BeConfDependentTest;
import org.openecomp.sdc.be.components.utils.ComponentInstancePropertyBuilder;
import org.openecomp.sdc.be.components.utils.ResourceBuilder;
import org.openecomp.sdc.be.components.utils.ServiceBuilder;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.PolicyDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;

import java.util.Map;
import java.util.Set;
import org.openecomp.sdc.be.model.operations.impl.UniqueIdBuilder;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;

public class PolicyUtilsTest extends BeConfDependentTest{

	private static final String PROP_NAME = "propertyName";
	private static final String COMP_ID = "compId";
	private static final String appConfigDir = "src/test/resources/config/catalog-be";
	private static final String EXPECTED_SERVICE_POLICY_TYPE = "a.b.c";
	private static final String EXPECTED_RESOURCE_POLICY_TYPE = "c.d.e";
	private static final String POLICY_ID_1 = "policyId1";
	private static final String POLICY_ID_2 = "policyId2";
	private static final String POLICY_NAME = "policyName";

	@Test
	public void testGetNextPolicyCounter() throws Exception {
		Map<String, PolicyDefinition> policies = null;
		int result;

		// default test
		result = PolicyUtils.getNextPolicyCounter(policies);
	}

	@Test
	public void testValidatePolicyFields() throws Exception {
		PolicyDefinition recievedPolicy = new PolicyDefinition();
		PolicyDefinition validPolicy = new PolicyDefinition();
		Map<String, PolicyDefinition> policies = null;
		Either<PolicyDefinition, ActionStatus> result;

		// default test
		result = PolicyUtils.validatePolicyFields(recievedPolicy, validPolicy, policies);
	}

	@Test
	public void testValidatePolicyFieldsOneEmptyField() {
		PolicyDefinition receivedPolicy = new PolicyDefinition();
		PolicyDefinition validPolicy = new PolicyDefinition();

		receivedPolicy.setName(POLICY_NAME);
		receivedPolicy.setUniqueId(null);
		validPolicy.setUniqueId(POLICY_ID_2);

		Either<PolicyDefinition, ActionStatus> policyEither =
				PolicyUtils.validatePolicyFields(receivedPolicy, validPolicy, null);

		assertTrue(policyEither.isLeft());
		assertEquals(validPolicy, policyEither.left().value());
	}

	@Test
	public void testValidatePolicyFieldsUpdateUniqueId() {
		PolicyDefinition receivedPolicy = new PolicyDefinition();
		PolicyDefinition validPolicy = new PolicyDefinition();

		receivedPolicy.setName(POLICY_NAME);
		receivedPolicy.setUniqueId(POLICY_ID_1);
		validPolicy.setUniqueId(POLICY_ID_2);

		Either<PolicyDefinition, ActionStatus> policyEither =
				PolicyUtils.validatePolicyFields(receivedPolicy, validPolicy, null);

		assertTrue(policyEither.isLeft());
		assertEquals(validPolicy, policyEither.left().value());
	}

	@Test
	public void testGetExcludedPolicyTypesByComponent() throws Exception {
		Component component = new Resource();
		Set<String> result;

		// default test
		result = PolicyUtils.getExcludedPolicyTypesByComponent(component);
		component = new Service();
		result = PolicyUtils.getExcludedPolicyTypesByComponent(component);
	}

	@Test
	public void testGetExcludedPoliciesWithServiceComponent() {
		ConfigurationSource configurationSource = new FSConfigurationSource(ExternalConfiguration.getChangeListener(), appConfigDir);
		ConfigurationManager configurationManager = new ConfigurationManager(configurationSource);

		Service service = new ServiceBuilder().setUniqueId(COMP_ID).build();

		Set<String> policyTypes = PolicyUtils.getExcludedPolicyTypesByComponent(service);
		validateExtractedPolicies(policyTypes, EXPECTED_SERVICE_POLICY_TYPE);
	}

	@Test
	public void testGetExcludedPoliciesWithResourceComponent() {
		ConfigurationSource configurationSource = new FSConfigurationSource(ExternalConfiguration.getChangeListener(), appConfigDir);
		ConfigurationManager configurationManager = new ConfigurationManager(configurationSource);

		Resource resource = new ResourceBuilder().setUniqueId(COMP_ID).build();

		Set<String> policyTypes = PolicyUtils.getExcludedPolicyTypesByComponent(resource);
		validateExtractedPolicies(policyTypes, EXPECTED_RESOURCE_POLICY_TYPE);
	}

	@Test
	public void testExtractNextPolicyCounterFromUniqueId() throws Exception {
		String uniqueId = "";
		int result;

		// default test
		result = Deencapsulation.invoke(PolicyUtils.class, "extractNextPolicyCounterFromUniqueId",
				new Object[] { uniqueId });
	}

	@Test
	public void testExtractNextPolicyCounterFromName() throws Exception {
		String policyName = "";
		int result;

		// default test
		result = Deencapsulation.invoke(PolicyUtils.class, "extractNextPolicyCounterFromName",
				new Object[] { policyName });
	}

	@Test
	public void testExtractNextPolicyCounter() throws Exception {
		String policyName = "";
		int endIndex = 0;
		int result;

		// default test
		result = Deencapsulation.invoke(PolicyUtils.class, "extractNextPolicyCounter",
				new Object[] { policyName, endIndex });
	}

	@Test
	public void testValidateImmutablePolicyFields() throws Exception {
		PolicyDefinition receivedPolicy = new PolicyDefinition();
		PolicyDefinition validPolicy = new PolicyDefinition();

		// default test
		Deencapsulation.invoke(PolicyUtils.class, "validateImmutablePolicyFields",
				receivedPolicy, validPolicy);
	}

	@Test
	public void testIsUpdatedField() throws Exception {
		String oldField = "";
		String newField = "";
		boolean result;

		// default test
		result = Deencapsulation.invoke(PolicyUtils.class, "isUpdatedField", new Object[] { oldField, newField });
	}

	@Test
	public void testLogImmutableFieldUpdateWarning() throws Exception {
		String oldValue = "";
		String newValue = "";
		JsonPresentationFields field = null;

		// default test
		Deencapsulation.invoke(PolicyUtils.class, "logImmutableFieldUpdateWarning",
				new Object[] { oldValue, newValue, JsonPresentationFields.class });
	}

	@Test
	public void testGetDeclaredPolicyDefinition() {
		ComponentInstanceProperty property = new ComponentInstancePropertyBuilder().setName(PROP_NAME).build();
		PolicyDefinition policy = PolicyUtils.getDeclaredPolicyDefinition(COMP_ID, property);

		assertTrue(Objects.nonNull(policy));
		assertEquals(UniqueIdBuilder.buildPolicyUniqueId(COMP_ID, PROP_NAME), policy.getUniqueId());
		assertEquals(COMP_ID, policy.getInstanceUniqueId());
	}

	private void validateExtractedPolicies(Set<String> policyTypes, String expectedType) {
		assertTrue(org.apache.commons.collections.CollectionUtils.isNotEmpty(policyTypes));
		assertEquals(1, policyTypes.size());
		assertEquals(expectedType, policyTypes.iterator().next());
	}
}