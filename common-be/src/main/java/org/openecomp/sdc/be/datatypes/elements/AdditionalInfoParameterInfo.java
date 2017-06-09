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

package org.openecomp.sdc.be.datatypes.elements;

import java.io.Serializable;

import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;

public class AdditionalInfoParameterInfo extends ToscaDataDefinition implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2066876282722907709L;

	String uniqueId;
	String key;
	String value;

	public AdditionalInfoParameterInfo() {
		super();
	}

	public AdditionalInfoParameterInfo(String key, String value) {
		super();
		this.key = key;
		this.value = value;
	}

	public AdditionalInfoParameterInfo(String uniqueId, String key, String value) {
		super();
		this.uniqueId = uniqueId;
		this.key = key;
		this.value = value;
	}

	public String getUniqueId() {
		return uniqueId;
	}

	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "AdditionalInfoParameterInfo [uniqueId=" + uniqueId + ", key=" + key + ", value=" + value + "]";
	}

}
