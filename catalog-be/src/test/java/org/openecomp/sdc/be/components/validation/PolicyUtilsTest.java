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

import fj.data.Either;
import mockit.Deencapsulation;
import org.junit.Test;
import org.openecomp.sdc.be.components.BeConfDependentTest;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.PolicyDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;

import java.util.Map;
import java.util.Set;

public class PolicyUtilsTest extends BeConfDependentTest{

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
	public void testGetExcludedPolicyTypesByComponent() throws Exception {
		Component component = new Resource();
		Set<String> result;

		// default test
		result = PolicyUtils.getExcludedPolicyTypesByComponent(component);
		component = new Service();
		result = PolicyUtils.getExcludedPolicyTypesByComponent(component);
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
}