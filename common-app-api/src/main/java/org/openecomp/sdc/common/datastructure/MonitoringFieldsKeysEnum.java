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

package org.openecomp.sdc.common.datastructure;

import java.util.Date;

public enum MonitoringFieldsKeysEnum {
	MONITORING_TIMESTAMP(Date.class, "TIMESTAMP"), 
	MONITORING_HOST_IP(String.class, "HOST_IP"), 
	MONITORING_HOST_CPU(Long.class, "HOST_CPU"), 
	MONITORING_HOST_MEM(Long.class, "HOST_MEMORY"), 
	MONITORING_HOST_DISC(Long.class, "HOST_DISC"), 
	MONITORING_JVM_ID(String.class, "JVM_ID"), 
	MONITORING_JVM_CPU(Long.class, "JVM_CPU"), 
	MONITORING_JVM_MEM(Long.class, "JVM_MEMORY"), 
	MONITORING_JVM_TNUM(Long.class, "JVM_TNUM"), 
	MONITORING_APP_ID(String.class, "APP_ID"), 
	MONITORING_APP_STAT(String.class, "APP_STAT");

	private Class<?> clazz;
	private String displayName;

	MonitoringFieldsKeysEnum(Class<?> clazz, String displayName) {
		this.clazz = clazz;
		this.displayName = displayName;
	}

	public Class<?> getValueClass() {
		return this.clazz;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
}
