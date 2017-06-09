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

package org.openecomp.sdc.be.model.tosca.validators;

import java.util.Map;

import org.openecomp.sdc.be.model.DataTypeDefinition;

public interface PropertyTypeValidator {

	boolean isValid(String value, String innerType, Map<String, DataTypeDefinition> allDataTypes);

	boolean isValid(String value, String innerType);
	/*
	 * The value format should be validated according to the "Property Type" :
	 * "integer" - valid tag:yaml.org,2002:int , the number base 8,10,18 should
	 * be handled ( hint : to validate by calling parseInt(
	 * s,10)/parseInt(s,16)/parseInt(s,8) or just regexp [-+]?[0-9]+ for Base 10
	 * , [-+]?0[0-7]+ for Base 8 , [-+]?0x[0-9a-fA-F]+ for Base 16
	 * 
	 * "float" - valid tag:yaml.org,2002:float , parseFloat() "boolean" - valid
	 * tag:yaml.org,2002:bool : can be only "true" or "false" ( upper case
	 * characters should be converted to lower case : TRUE ->true, True->true
	 * "string" - valid tag:yaml.org,2002:str and limited to 100 chars.
	 * 
	 */

}
