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

package org.openecomp.sdc.asdctool.impl.validator.executers;

import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NodeToscaArtifactsValidatorExecuter extends ArtifactValidatorExecuter implements IArtifactValidatorExecuter{
	 protected String name;
	
	 public NodeToscaArtifactsValidatorExecuter(JanusGraphDao janusGraphDao,
		 ToscaOperationFacade toscaOperationFacade) {
		 			super(janusGraphDao, toscaOperationFacade);
	        setName("RESOURCE_TOSCA_ARTIFACTS");
	    }
	@Override
	public boolean executeValidations() {
		
		Map<GraphPropertyEnum, Object> hasProps = new HashMap<>();
		hasProps.put(GraphPropertyEnum.COMPONENT_TYPE, ComponentTypeEnum.RESOURCE.name());		
		hasProps.put(GraphPropertyEnum.STATE, LifecycleStateEnum.CERTIFIED.name());		
		
		Map<String, List<Component>> vertices = getVerticesToValidate(VertexTypeEnum.NODE_TYPE, hasProps);
        return validate(vertices);
		
	}

	@Override
	public String getName() {		
		return name;
	}	
	

	public void setName(String name) {
		this.name = name;
	}

}
