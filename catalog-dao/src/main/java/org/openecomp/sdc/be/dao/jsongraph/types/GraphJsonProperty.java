package org.openecomp.sdc.be.dao.jsongraph.types;

import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;

public enum GraphJsonProperty {
	METADATA	(GraphPropertyEnum.METADATA),
	JSON		(GraphPropertyEnum.JSON);
	
	private GraphPropertyEnum propInGraph;
	
	GraphJsonProperty (GraphPropertyEnum propInGraph){
		this.propInGraph = propInGraph;
	}

	public GraphPropertyEnum getPropInGraph() {
		return propInGraph;
	}
	
}
