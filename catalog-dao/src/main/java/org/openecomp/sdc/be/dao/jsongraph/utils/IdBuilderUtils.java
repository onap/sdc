package org.openecomp.sdc.be.dao.jsongraph.utils;

import java.util.UUID;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;

public class IdBuilderUtils {
	private static String DOT = ".";
	
	public static String generateChildId(String componentId, VertexTypeEnum type){
		StringBuffer sb = new StringBuffer(componentId);
		sb.append(DOT).append(type.getName());
		return sb.toString();
	}
	
	public static String generateUUID(){
		return UUID.randomUUID().toString();
	}
	
	public static String generateUniqueId(){
		return UUID.randomUUID().toString();
	}

}
