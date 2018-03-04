package org.openecomp.sdc.asdctool.impl.validator.executers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.LifecycleStateEnum;

public class VFToscaArtifactValidatorExecutor extends ArtifactValidatorExecuter implements IArtifactValidatorExecuter{
	
	public VFToscaArtifactValidatorExecutor() {
	        setName("VF_TOSCA_ARTIFACTS");
	    }
	@Override
	public boolean executeValidations() {
		
		Map<GraphPropertyEnum, Object> hasProps = new HashMap<>();
		hasProps.put(GraphPropertyEnum.COMPONENT_TYPE, ComponentTypeEnum.RESOURCE.name());	
		hasProps.put(GraphPropertyEnum.RESOURCE_TYPE, ResourceTypeEnum.VF);
		hasProps.put(GraphPropertyEnum.STATE, LifecycleStateEnum.CERTIFIED.name());		
		
		Map<String, List<Component>> vertices = getVerticesToValidate(VertexTypeEnum.TOPOLOGY_TEMPLATE, hasProps);
		return validate( vertices);
		
	}

	@Override
	public String getName() {		
		return name;
	}	
	

	public void setName(String name) {
		this.name = name;
	}
	

}
