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

package org.openecomp.sdc.common.test.config;

import static java.lang.String.format;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.openecomp.sdc.common.api.BasicConfiguration;

public class TestConfiguration extends BasicConfiguration {

	/**
	 * backend host
	 */
	private String beHost;
	/**
	 * backend http port
	 */
	private Integer beHttpPort;
	/**
	 * backend http secured port
	 */
	private Integer beSslPort;
	/**
	 * be http context
	 */
	private String beContext;
	/**
	 * backend protocol. http | https
	 */
	private String beProtocol = "http";

	private Date released;
	private String version = "1111";
	private TestConnection connection;
	private List<String> protocols;
	private Map<String, String> users;

	public Date getReleased() {
		return released;
	}

	public String getVersion() {
		return version;
	}

	public void setReleased(Date released) {
		this.released = released;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public TestConnection getConnection() {
		return connection;
	}

	public void setConnection(TestConnection connection) {
		this.connection = connection;
	}

	public List<String> getProtocols() {
		return protocols;
	}

	public void setProtocols(List<String> protocols) {
		this.protocols = protocols;
	}

	public Map<String, String> getUsers() {
		return users;
	}

	public void setUsers(Map<String, String> users) {
		this.users = users;
	}

	public String getBeHost() {
		return beHost;
	}

	public void setBeHost(String beHost) {
		this.beHost = beHost;
	}

	public Integer getBeHttpPort() {
		return beHttpPort;
	}

	public void setBeHttpPort(Integer beHttpPort) {
		this.beHttpPort = beHttpPort;
	}

	public Integer getBeSslPort() {
		return beSslPort;
	}

	public void setBeSslPort(Integer beSslPort) {
		this.beSslPort = beSslPort;
	}

	public String getBeContext() {
		return beContext;
	}

	public void setBeContext(String beContext) {
		this.beContext = beContext;
	}

	public String getBeProtocol() {
		return beProtocol;
	}

	public void setBeProtocol(String beProtocol) {
		this.beProtocol = beProtocol;
	}

	@Override
	public String toString() {
		return new StringBuilder().append(format("backend host: %s\n", beHost))
				.append(format("backend http port: %s\n", beHttpPort))
				.append(format("backend ssl port: %s\n", beSslPort)).append(format("backend context: %s\n", beContext))
				.append(format("backend protocol: %s\n", beProtocol)).append(format("Version: %s\n", version))
				.append(format("Released: %s\n", released)).append(format("Connecting to database: %s\n", connection))
				.append(format("Supported protocols: %s\n", protocols)).append(format("Users: %s\n", users)).toString();
	}
}
