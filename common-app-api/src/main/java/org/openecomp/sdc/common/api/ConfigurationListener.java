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

package org.openecomp.sdc.common.api;

public class ConfigurationListener {

	private Class<? extends BasicConfiguration> type;
	private FileChangeCallback callBack;

	public ConfigurationListener(Class<? extends BasicConfiguration> type, FileChangeCallback callBack) {
		super();
		this.type = type;
		this.callBack = callBack;
	}

	public Class<? extends BasicConfiguration> getType() {
		return type;
	}

	public void setType(Class<? extends BasicConfiguration> type) {
		this.type = type;
	}

	public FileChangeCallback getCallBack() {
		return callBack;
	}

	public void setCallBack(FileChangeCallback callBack) {
		this.callBack = callBack;
	}

}
