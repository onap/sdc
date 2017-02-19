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

package org.openecomp.sdc.be.components.clean;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openecomp.sdc.be.components.impl.BaseBusinessLogic;
import org.openecomp.sdc.be.components.impl.ComponentBusinessLogic;
import org.openecomp.sdc.be.components.impl.ResourceBusinessLogic;
import org.openecomp.sdc.be.components.impl.ServiceBusinessLogic;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fj.data.Either;

@Component("componentsCleanBusinessLogic")
public class ComponentsCleanBusinessLogic extends BaseBusinessLogic {

	@Autowired
	private ResourceBusinessLogic resourceBusinessLogic;

	@Autowired
	private ServiceBusinessLogic serviceBusinessLogic;

	private static Logger log = LoggerFactory.getLogger(ComponentsCleanBusinessLogic.class.getName());

	public Map<NodeTypeEnum, Either<List<String>, ResponseFormat>> cleanComponents(List<NodeTypeEnum> componentsToClean) {

		Map<NodeTypeEnum, Either<List<String>, ResponseFormat>> cleanedComponents = new HashMap<NodeTypeEnum, Either<List<String>, ResponseFormat>>();

		log.trace("start cleanComponents");
		for (NodeTypeEnum type : componentsToClean) {
			switch (type) {
			case Resource:
				processDeletionForType(cleanedComponents, NodeTypeEnum.Resource, resourceBusinessLogic);
				break;
			case Service:
				processDeletionForType(cleanedComponents, NodeTypeEnum.Service, serviceBusinessLogic);
				break;
			default:
				log.debug("{} component type does not have cleaning method defined", type);
				break;
			}
		}

		log.trace("end cleanComponents");
		return cleanedComponents;
	}

	private void processDeletionForType(Map<NodeTypeEnum, Either<List<String>, ResponseFormat>> cleanedComponents, NodeTypeEnum type, ComponentBusinessLogic componentBusinessLogic) {
		Either<List<String>, ResponseFormat> deleteMarkedResources = componentBusinessLogic.deleteMarkedComponents();
		if (deleteMarkedResources.isRight()) {
			log.debug("failed to clean deleted components of type {}. error: {}", type, deleteMarkedResources.right().value().getFormattedMessage());
		} else {
			if (log.isDebugEnabled()) {
				StringBuilder sb = new StringBuilder("list of deleted components - type " + type + ": ");
				for (String id : deleteMarkedResources.left().value()) {
					sb.append(id).append(", ");
				}
				log.debug(sb.toString());
			}
		}
		cleanedComponents.put(type, deleteMarkedResources);
	}
}
