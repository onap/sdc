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

package org.openecomp.sdc.be.model;

public class GetInputValueInfo {
	String propName;
	String inputName;
	Integer indexValue;
	GetInputValueInfo getInputIndex;

	boolean isList = false;

	public String getPropName() {
		return propName;
	}

	public void setPropName(String propName) {
		this.propName = propName;
	}

	public String getInputName() {
		return inputName;
	}

	public void setInputName(String inputName) {
		this.inputName = inputName;
	}

	public Integer getIndexValue() {
		return indexValue;
	}

	public void setIndexValue(Integer indexValue) {
		this.indexValue = indexValue;
	}

	public GetInputValueInfo getGetInputIndex() {
		return getInputIndex;
	}

	public void setGetInputIndex(GetInputValueInfo getInputIndex) {
		this.getInputIndex = getInputIndex;
	}

	public boolean isList() {
		return isList;
	}

	public void setList(boolean isList) {
		this.isList = isList;
	}

}
