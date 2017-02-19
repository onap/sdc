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

package org.openecomp.sdc.common.monitoring;

import org.openecomp.sdc.common.datastructure.ESTimeBasedEvent;

public class MonitoringEvent extends ESTimeBasedEvent {

	private static String MONITORING_EVENT_TEMPLATE = "hostid=\"%s\" hostcpu=\"%s\" hostmem=\"%s\" hostdisk=\"%s\" "
			+ "jvmid=\"%s\" jvmcpu=\"%s\" jvmmem=\"%s\" jvmtnum=\"%s\" " + "appid=\"%s\" appstat=\"%s\"";

	private String hostid;
	private Long hostcpu;
	private Double hostmem;
	private String hostdisk;
	private String jvmid;
	private Long jvmcpu;
	private Long jvmmem;
	private Integer jvmtnum;
	private String appid;
	private String appstat;

	public String getHostid() {
		return hostid;
	}

	public void setHostid(String hostid) {
		this.hostid = hostid;
	}

	public Long getHostcpu() {
		return hostcpu;
	}

	public void setHostcpu(Long hostcpu) {
		this.hostcpu = hostcpu;
	}

	public Double getHostmem() {
		return hostmem;
	}

	public void setHostmem(Double hostmem) {
		this.hostmem = hostmem;
	}

	public String getHostdisk() {
		return hostdisk;
	}

	public void setHostdisk(String hostdisk) {
		this.hostdisk = hostdisk;
	}

	public String getJvmid() {
		return jvmid;
	}

	public void setJvmid(String jvmid) {
		this.jvmid = jvmid;
	}

	public Long getJvmcpu() {
		return jvmcpu;
	}

	public void setJvmcpu(Long jvmcpu) {
		this.jvmcpu = jvmcpu;
	}

	public Long getJvmmem() {
		return jvmmem;
	}

	public void setJvmmem(Long jvmmem) {
		this.jvmmem = jvmmem;
	}

	public Integer getJvmtnum() {
		return jvmtnum;
	}

	public void setJvmtnum(Integer jvmtnum) {
		this.jvmtnum = jvmtnum;
	}

	public String getAppid() {
		return appid;
	}

	public void setAppid(String appid) {
		this.appid = appid;
	}

	public String getAppstat() {
		return appstat;
	}

	public void setAppstat(String appstat) {
		this.appstat = appstat;
	}

	@Override
	public String toString() {
		return getFormattedString(MONITORING_EVENT_TEMPLATE, hostid, hostcpu, hostmem, hostdisk, jvmid, jvmcpu, jvmmem,
				jvmtnum, appid, appstat);
	}
}
