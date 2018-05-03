package org.openecomp.sdc.asdctool.impl.validator.executers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.LifecycleStateEnum;

public class ServiceToscaArtifactsValidatorExecutor extends ArtifactValidatorExecuter implements IArtifactValidatorExecuter{
	 
		
	 public ServiceToscaArtifactsValidatorExecutor() {
	        setName("SERVICE_TOSCA_ARTIFACTS");
	    }
	@Override
	public boolean executeValidations() {
		Map<GraphPropertyEnum, Object> hasProps = new HashMap<>();
		hasProps.put(GraphPropertyEnum.COMPONENT_TYPE, ComponentTypeEnum.SERVICE.name());
		hasProps.put(GraphPropertyEnum.STATE, LifecycleStateEnum.CERTIFIED.name());
		
		Map<String, List<Component>> vertices = getVerticesToValidate(VertexTypeEnum.TOPOLOGY_TEMPLATE, hasProps);
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
