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

import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.operations.impl.ComponentOperation;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import fj.data.Either;

@Component("vfComponentInstanceBusinessLogic")
public class VFComponentInstanceBusinessLogic extends ComponentInstanceBusinessLogic {

	private static Logger log = LoggerFactory.getLogger(VFComponentInstanceBusinessLogic.class.getName());

	@Override
	protected Either<Boolean, ResponseFormat> validateAllowedToContainCompInstances(org.openecomp.sdc.be.model.Component containerComponent) {
		Resource resource = (Resource) containerComponent;
		ResourceTypeEnum resourceType = resource.getResourceType();
		if (ResourceTypeEnum.VF != resourceType) {
			log.debug("Cannot attach resource instances to container resource of type {}", resourceType);
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.RESOURCE_CANNOT_CONTAIN_RESOURCE_INSTANCES, resourceType.getValue()));
		}
		return Either.left(true);
	}

	@Override
	protected NodeTypeEnum getNodeTypeOfComponentInstanceOrigin() {
		return NodeTypeEnum.Resource;
	}

	@Override
	protected ComponentOperation getContainerComponentOperation() {
		return resourceOperation;
	}

	@Override
	protected ComponentOperation getCompInstOriginComponentOperation() {
		return resourceOperation;
	}

	@Override
	protected ComponentTypeEnum getComponentTypeOfComponentInstance() {
		return ComponentTypeEnum.RESOURCE_INSTANCE;
	}

}
