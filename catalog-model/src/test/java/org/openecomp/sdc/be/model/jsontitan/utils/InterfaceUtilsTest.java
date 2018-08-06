package org.openecomp.sdc.be.model.jsontitan.utils;

import org.junit.Test;
import org.openecomp.sdc.be.model.InterfaceDefinition;
import org.openecomp.sdc.be.model.Operation;
import org.openecomp.sdc.be.model.Resource;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InterfaceUtilsTest {

	private InterfaceUtils createTestSubject() {
		return new InterfaceUtils();
	}

	
	@Test
	public void testGetInterfaceDefinitionFromToscaName() throws Exception {
		Collection<InterfaceDefinition> interfaces = null;
		String resourceName = "";
		Optional<InterfaceDefinition> result;

		// default test
		result = InterfaceUtils.getInterfaceDefinitionFromToscaName(interfaces, resourceName);
	}

	
	@Test
	public void testGetInterfaceDefinitionListFromToscaName() throws Exception {
		Collection<InterfaceDefinition> interfaces = null;
		String resourceName = "";
		Collection<InterfaceDefinition> result;

		// default test
		result = InterfaceUtils.getInterfaceDefinitionListFromToscaName(interfaces, resourceName);
	}

	
	@Test
	public void testCreateInterfaceToscaResourceName() throws Exception {
		String resourceName = "";
		String result;

		// default test
		result = InterfaceUtils.createInterfaceToscaResourceName(resourceName);
	}
}