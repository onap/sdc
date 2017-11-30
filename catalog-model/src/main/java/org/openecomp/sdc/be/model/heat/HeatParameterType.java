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

package org.openecomp.sdc.be.model.heat;

import org.openecomp.sdc.be.model.tosca.converters.HeatBooleanConverter;
import org.openecomp.sdc.be.model.tosca.converters.HeatCommaDelimitedListConverter;
import org.openecomp.sdc.be.model.tosca.converters.HeatJsonConverter;
import org.openecomp.sdc.be.model.tosca.converters.HeatNumberConverter;
import org.openecomp.sdc.be.model.tosca.converters.HeatStringConverter;
import org.openecomp.sdc.be.model.tosca.converters.PropertyValueConverter;
import org.openecomp.sdc.be.model.tosca.validators.HeatBooleanValidator;
import org.openecomp.sdc.be.model.tosca.validators.HeatCommaDelimitedListValidator;
import org.openecomp.sdc.be.model.tosca.validators.HeatNumberValidator;
import org.openecomp.sdc.be.model.tosca.validators.HeatStringValidator;
import org.openecomp.sdc.be.model.tosca.validators.PropertyTypeValidator;

public enum HeatParameterType {

	STRING("string", HeatStringValidator.getInstance(), HeatStringConverter.getInstance()),

	BOOLEAN("boolean", HeatBooleanValidator.getInstance(), HeatBooleanConverter.getInstance()),

	NUMBER("number", HeatNumberValidator.getInstance(), HeatNumberConverter.getInstance()),

	JSON("json", HeatStringValidator.getInstance(), HeatJsonConverter.getInstance()),

	COMMA_DELIMITED_LIST("comma_delimited_list", HeatCommaDelimitedListValidator.getInstance(),
			HeatCommaDelimitedListConverter.getInstance());

	private String type;
	private PropertyTypeValidator validator;
	private PropertyValueConverter converter;

	HeatParameterType(String type, PropertyTypeValidator validator, PropertyValueConverter converter) {
		this.type = type;
		this.validator = validator;
		this.converter = converter;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public PropertyTypeValidator getValidator() {
		return validator;
	}

	public void setValidator(PropertyTypeValidator validator) {
		this.validator = validator;
	}

	public PropertyValueConverter getConverter() {
		return converter;
	}

	public void setConverter(PropertyValueConverter converter) {
		this.converter = converter;
	}

	public static HeatParameterType isValidType(String typeName) {
		if (typeName == null) {
			return null;
		}

		for (HeatParameterType type : HeatParameterType.values()) {
			if (type.getType().equals(typeName)) {
				return type;
			}
		}
		return null;
	}
}
