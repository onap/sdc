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
