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

package org.openecomp.sdc.ci.tests.datatypes;

public class HeatMetaFirstLevelDefinition {
	
	public HeatMetaFirstLevelDefinition(String fileName, String type, String checkSum) {
		super();
		this.fileName = fileName;
		this.type = type;
		this.checkSum = checkSum;
		
	}
	
	private String fileName;
	private String type;
	private String checkSum;
	
	public HeatMetaFirstLevelDefinition() {
		super();
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}


	public String getCheckSum() {
		return checkSum;
	}

	public void setCheckSum(String checkSum) {
		this.checkSum = checkSum;
	}

	@Override
	public String toString() {
		return "HeatMetaFirstLevelDefinition [fileName=" + fileName + ", type=" + type + ", checkSum=" + checkSum + "]";
	}
	
	
	
	

}
