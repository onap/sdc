/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.be.components;

import java.util.List;

import org.junit.Test;
import org.openecomp.sdc.be.components.impl.ComponentBusinessLogic;
import org.openecomp.sdc.be.components.impl.ComponentInstanceBusinessLogic;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.ui.model.UiComponentDataTransfer;
import org.openecomp.sdc.exception.ResponseFormat;

import fj.data.Either;

public class ComponentBusinessLogicTest {
	ComponentBusinessLogic businessLogic = new ComponentBusinessLogic() {

		@Override
		public Either<List<String>, ResponseFormat> deleteMarkedComponents() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public ComponentInstanceBusinessLogic getComponentInstanceBL() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Either<List<ComponentInstance>, ResponseFormat> getComponentInstancesFilteredByPropertiesAndInputs(String componentId, ComponentTypeEnum componentTypeEnum, String userId, String searchText) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Either<UiComponentDataTransfer, ResponseFormat> getUiComponentDataTransferByComponentId(String componentId, List<String> dataParamsToReturn) {
			// TODO Auto-generated method stub
			return null;
		}
	};

	@Test
	public void testGetRequirementsAndCapabilities() {
		// businessLogic.getRequirementsAndCapabilities(componentId,
		// componentTypeEnum);
	}
}
