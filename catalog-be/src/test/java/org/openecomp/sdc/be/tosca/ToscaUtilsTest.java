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

package org.openecomp.sdc.be.tosca;

import org.junit.Test;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.Resource;

import java.util.Map;

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
