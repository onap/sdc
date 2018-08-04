package org.openecomp.sdc.be.tosca;

import java.util.Map;

import org.junit.Test;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.Resource;

public class ToscaUtilsTest {
	
	@Test
	public void testIsComplexVfc() throws Exception {
		Component component = new Resource();		
		component.setComponentType(ComponentTypeEnum.RESOURCE);
		boolean result;

		// default test
		ToscaUtils.isNotComplexVfc(component);
	}

	
	@Test
	public void testObjectToMap() throws Exception {
		Object objectToConvert = null;
		Object obj = new Object();
		Map<String, Object> result;

		// default test
		ToscaUtils.objectToMap(objectToConvert, obj.getClass());
	}
}