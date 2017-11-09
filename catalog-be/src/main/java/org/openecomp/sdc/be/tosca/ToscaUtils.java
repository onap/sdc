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

package org.openecomp.sdc.be.tosca;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openecomp.sdc.be.datatypes.components.ResourceMetadataDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.Component;

public class ToscaUtils {

	public static boolean isComplexVfc(Component component) {
		if (ComponentTypeEnum.RESOURCE == component.getComponentType()) {
			ResourceTypeEnum resourceType = ((ResourceMetadataDataDefinition) component.getComponentMetadataDefinition().getMetadataDataDefinition()).getResourceType();
			if (ResourceTypeEnum.CVFC == resourceType) {
				return true;
			}
		}
		return false;
	}

	public static Map<String, Object> objectToMap(Object objectToConvert, Class<?> clazz) throws IllegalArgumentException, IllegalAccessException {
		Map<String, Object> map = new HashMap<>();
		List<Field> fields = new ArrayList<>();

		fields = getAllFields(fields, clazz);

		for (Field field : fields) {
			field.setAccessible(true);
			map.put(field.getName(), field.get(objectToConvert));
		}
		return map;
	}

	public static List<Field> getAllFields(List<Field> fields, Class<?> type) {
		fields.addAll(Arrays.asList(type.getDeclaredFields()));

		if (type.getSuperclass() != null) {
			fields = getAllFields(fields, type.getSuperclass());
		}
		return fields;
	}
	
	public static class SubstituitionEntry{
		
		private String fullName = "";
		private String sourceName = "";
		private String owner = "";
		
		public SubstituitionEntry() {}

		public SubstituitionEntry(String fullName, String sourceName, String owner) {
			if(fullName != null)
				this.fullName = fullName;
			if(sourceName != null)
				this.sourceName = sourceName;
			if(owner != null)
				this.owner = owner;
		}

		public String getFullName() {
			return fullName;
		}

		public void setFullName(String fullName) {
			this.fullName = fullName;
		}

		public String getSourceName() {
			return sourceName;
		}

		public void setSourceName(String sourceName) {
			this.sourceName = sourceName;
		}

		public String getOwner() {
			return owner;
		}

		public void setOwner(String owner) {
			this.owner = owner;
		}
	}
		
}
