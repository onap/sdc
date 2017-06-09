package org.openecomp.sdc.be.model.jsontitan.datamodel;

import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;

public enum ToscaElementTypeEnum {
	NodeType("node_type"),
	TopologyTemplate("topology_template");
	
	String value;
	private ToscaElementTypeEnum(String value){
		this.value = value;
	}
	
	public static  VertexTypeEnum getVertexTypeByToscaType(ToscaElementTypeEnum toscaType ){
		switch ( toscaType ){
		case NodeType :
			return VertexTypeEnum.NODE_TYPE;
		case TopologyTemplate :
			return VertexTypeEnum.TOPOLOGY_TEMPLATE;
		default :
			return  null;
		}
	}

	public String getValue() {
		return value;
	}
	
}
