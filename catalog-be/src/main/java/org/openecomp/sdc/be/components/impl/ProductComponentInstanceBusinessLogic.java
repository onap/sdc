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

package org.openecomp.sdc.be.components.impl;

import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.springframework.stereotype.Component;

@Component("productComponentInstanceBusinessLogic")
public class ProductComponentInstanceBusinessLogic extends ComponentInstanceBusinessLogic {


	@Override
	protected NodeTypeEnum getNodeTypeOfComponentInstanceOrigin() {
		// TODO Pavel this might be also a resource?
		return NodeTypeEnum.Service;
	}

	@Override
	protected ComponentTypeEnum getComponentTypeOfComponentInstance() {
		// TODO Auto-generated method stub
		return ComponentTypeEnum.SERVICE_INSTANCE;
	}

}
