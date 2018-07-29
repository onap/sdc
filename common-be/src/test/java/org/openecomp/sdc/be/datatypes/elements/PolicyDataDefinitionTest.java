package org.openecomp.sdc.be.datatypes.elements;

import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PolicyDataDefinitionTest {

	private PolicyDataDefinition createTestSubject() {
		return new PolicyDataDefinition();
	}

	@Test
	public void testConstructors() throws Exception {
		PolicyDataDefinition testSubject;
		Boolean result;

		// default test
		testSubject = createTestSubject();
		new PolicyDataDefinition(new HashMap<>());
		new PolicyDataDefinition(testSubject);
	}
	
	@Test
	public void testGetIsFromCsar() throws Exception {
		PolicyDataDefinition testSubject;
		Boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getIsFromCsar();
	}

	@Test
	public void testGetComponentName() throws Exception {
		PolicyDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getComponentName();
	}

	@Test
	public void testSetComponentName() throws Exception {
		PolicyDataDefinition testSubject;
		String componentName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setComponentName(componentName);
	}

	@Test
	public void testGetInvariantName() throws Exception {
		PolicyDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getInvariantName();
	}

	@Test
	public void testSetInvariantName() throws Exception {
		PolicyDataDefinition testSubject;
		Object invariantName = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setInvariantName(invariantName);
	}

	@Test
	public void testGetName() throws Exception {
		PolicyDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getName();
	}

	@Test
	public void testSetName() throws Exception {
		PolicyDataDefinition testSubject;
		String name = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setName(name);
	}

	@Test
	public void testGetUniqueId() throws Exception {
		PolicyDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUniqueId();
	}

	@Test
	public void testSetUniqueId() throws Exception {
		PolicyDataDefinition testSubject;
		String uniqueId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setUniqueId(uniqueId);
	}

	@Test
	public void testGetPolicyTypeName() throws Exception {
		PolicyDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getPolicyTypeName();
	}

	@Test
	public void testSetPolicyTypeName() throws Exception {
		PolicyDataDefinition testSubject;
		String policyTypeName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setPolicyTypeName(policyTypeName);
	}

	@Test
	public void testGetPolicyTypeUid() throws Exception {
		PolicyDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getPolicyTypeUid();
	}

	@Test
	public void testSetPolicyTypeUid() throws Exception {
		PolicyDataDefinition testSubject;
		String policyTypeUid = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setPolicyTypeUid(policyTypeUid);
	}

	@Test
	public void testGetVersion() throws Exception {
		PolicyDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getVersion();
	}

	@Test
	public void testSetVersion() throws Exception {
		PolicyDataDefinition testSubject;
		String version = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setVersion(version);
	}

	@Test
	public void testGetDerivedFrom() throws Exception {
		PolicyDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDerivedFrom();
	}

	@Test
	public void testSetDerivedFrom() throws Exception {
		PolicyDataDefinition testSubject;
		String derivedFrom = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setDerivedFrom(derivedFrom);
	}

	@Test
	public void testGetDescription() throws Exception {
		PolicyDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDescription();
	}

	@Test
	public void testSetDescription() throws Exception {
		PolicyDataDefinition testSubject;
		String description = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setDescription(description);
	}

	@Test
	public void testGetPolicyUUID() throws Exception {
		PolicyDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getPolicyUUID();
	}

	@Test
	public void testSetPolicyUUID() throws Exception {
		PolicyDataDefinition testSubject;
		String policyUUID = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setPolicyUUID(policyUUID);
	}

	@Test
	public void testGetInvariantUUID() throws Exception {
		PolicyDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getInvariantUUID();
	}

	@Test
	public void testSetInvariantUUID() throws Exception {
		PolicyDataDefinition testSubject;
		String invariantUUID = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setInvariantUUID(invariantUUID);
	}

	@Test
	public void testGetProperties() throws Exception {
		PolicyDataDefinition testSubject;
		List<PropertyDataDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getProperties();
	}

	@Test
	public void testSetProperties() throws Exception {
		PolicyDataDefinition testSubject;
		List<PropertyDataDefinition> properties = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setProperties(properties);
	}

	@Test
	public void testGetTargets() throws Exception {
		PolicyDataDefinition testSubject;
		Map<PolicyTargetType, List<String>> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getTargets();
	}

	@Test
	public void testSetTargets() throws Exception {
		PolicyDataDefinition testSubject;
		Map<PolicyTargetType, List<String>> metadata = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setTargets(metadata);
	}

	@Test
	public void testResolveComponentInstanceTargets() throws Exception {
		PolicyDataDefinition testSubject;
		List<String> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.resolveComponentInstanceTargets();
	}

	@Test
	public void testContainsCmptInstanceAsTarget() throws Exception {
		PolicyDataDefinition testSubject;
		String cmptInstId = "";
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.containsTarget(cmptInstId,null);
	}
}