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

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.util.HashMap;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.hyperic.sigar.FileSystem;
import org.hyperic.sigar.Sigar;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MonitoringMetricsFetcher {

	private static Logger monitoringLogger = LoggerFactory.getLogger("asdc.fe.monitoring.fetcher");

	private static volatile MonitoringMetricsFetcher instance;
	private static RuntimeMXBean runtimeMXBean;
	private static ThreadMXBean threadMXBean;
	private static MemoryMXBean memoryMXBean;
	private static MBeanServer platformMBeanServer;
	private static Sigar sigarSession;

	private static String appName;
	private static String jvmName = "Unknown";
	private final String PROCESS_CPU_TIME_ATTR = "ProcessCpuTime";

	private static String FE_JVM_NAME = "jetty-fe";
	private static String BE_JVM_NAME = "jetty-be";

	private MonitoringMetricsFetcher() {
	};

	public static MonitoringMetricsFetcher getInstance() {
		if (instance == null) {
			instance = init();
		}
		return instance;
	}

	public MonitoringEvent getMonitoringMetrics() {
		MonitoringEvent monitoringEvent = new MonitoringEvent();
		monitoringEvent.setHostid(getFQDN());
		monitoringEvent.setHostcpu(getHostCpuTime());
		monitoringEvent.setHostmem(getHostUsedMemory());
		monitoringEvent.setHostdisk(getHostUsedDisk().toString());

		monitoringEvent.setJvmid(jvmName);

		monitoringEvent.setJvmcpu(getJvmCpuTime());
		monitoringEvent.setJvmmem(getJvmUsedHeapMemory());
		monitoringEvent.setJvmtnum(getJvmThreads());

		monitoringEvent.setAppid(appName);
		// this is probably from healthcheck
		// TODO
		monitoringEvent.setAppstat("appStatus");
		return monitoringEvent;
	}

	private static synchronized MonitoringMetricsFetcher init() {
		if (instance == null) {
			instance = new MonitoringMetricsFetcher();
			threadMXBean = ManagementFactory.getThreadMXBean();
			memoryMXBean = ManagementFactory.getMemoryMXBean();
			runtimeMXBean = ManagementFactory.getRuntimeMXBean();
			platformMBeanServer = ManagementFactory.getPlatformMBeanServer();
			sigarSession = new Sigar();
			appName = ExternalConfiguration.getAppName();
			monitoringLogger.debug("appName is {}", appName);
			// Accoridng to Yaki, there is no "calculated" jvmName like it was
			// in TAS,
			// just "jetty-be" or "jetty-fe"
			if (appName.contains("fe")) {
				jvmName = FE_JVM_NAME;
			} else if (appName.contains("be")) {
				jvmName = BE_JVM_NAME;
			} else {
				monitoringLogger
						.warn("Couldn't determine jvmName, appName is expected to contain \"be\" or \"fe\" string");
			}
		}
		return instance;
	}

	/**
	 * Returns the number of live threads for this JVM
	 * 
	 * @return number of live threads
	 */
	private Integer getJvmThreads() {
		return threadMXBean.getThreadCount();
	}

	/**
	 * Returns the number of used heap memory (bytes)
	 * 
	 * @return the number of used heap memory (bytes)
	 */
	private long getJvmUsedHeapMemory() {
		return memoryMXBean.getHeapMemoryUsage().getUsed();
	}

	/**
	 * Returns the jvm cpu time (msec)
	 * 
	 * @return the jvm cpu time (msec)
	 */
	private long getJvmCpuTime() {

		long cpuTime = -1;
		try {
			cpuTime = (long) platformMBeanServer.getAttribute(
					new ObjectName(ManagementFactory.OPERATING_SYSTEM_MXBEAN_NAME), PROCESS_CPU_TIME_ATTR);
		} catch (Exception e) {
			monitoringLogger.error("Couldn't measure JVM CPU time, error: {}", e);
		}
		return cpuTime;
	}

	/**
	 * Returns the host total cpu time (msec)
	 * 
	 * @return the host total cpu time (msec)
	 */
	private long getHostCpuTime() {
		long cpuTime = -1;
		try {
			cpuTime = sigarSession.getCpu().getTotal();
		} catch (Exception e) {
			monitoringLogger.error("Couldn't measure host CPU time, error: {}", e);
		}
		return cpuTime;
	}

	/**
	 * Returns the host used memory(msec)
	 * 
	 * @return the host used memory(msec)
	 */
	private Double getHostUsedMemory() {
		Double memory = -1.0;
		try {
			memory = sigarSession.getMem().getUsedPercent();
		} catch (Exception e) {
			monitoringLogger.error("Couldn't measure host used memory, error: {}", e);
		}
		return memory;
	}

	/**
	 * Returns the percentage of all available FS
	 * 
	 * @return the host avail disk(bytes)
	 */
	private Map<String, Double> getHostUsedDisk() {
		Map<String, Double> res = new HashMap<>();
		try {
			FileSystem[] fileSystemList = sigarSession.getFileSystemList();
			for (FileSystem fileSystem : fileSystemList) {

				String dirName = fileSystem.getDirName();
				double usePercent = sigarSession.getFileSystemUsage(dirName).getUsePercent() * 100;
				res.put(dirName, usePercent);
			}
		} catch (Exception e) {
			monitoringLogger.error("Couldn't measure host used disk, error: {}", e);
		}
		return res;
	}

	/**
	 * Returns the FQDN
	 * 
	 * @return the FQDN
	 */
	private String getFQDN() {
		String fqdn = "";
		try {
			fqdn = sigarSession.getFQDN();
		} catch (Exception e) {
			monitoringLogger.error("Couldn't get FQDN, error: {}", e);
		}
		return fqdn;
	}
}
