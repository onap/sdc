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

package org.openecomp.sdc.be.dao.rest;

public class RestConfigurationInfo {

	private Integer readTimeoutInSec = null;

	private Boolean ignoreCertificate = null;

	private Integer connectionPoolSize = null;

	private Integer connectTimeoutInSec = null;

	private Integer socketTimeoutInSec = null;

	/**
	 * @return the readTimeoutInSec
	 */
	public Integer getReadTimeoutInSec() {
		return readTimeoutInSec;
	}

	/**
	 * @param readTimeoutInSec
	 *            the readTimeoutInSec to set
	 */
	public void setReadTimeoutInSec(Integer readTimeoutInSec) {
		this.readTimeoutInSec = readTimeoutInSec;
	}

	/**
	 * @return the ignoreCertificate
	 */
	public Boolean getIgnoreCertificate() {
		return ignoreCertificate;
	}

	/**
	 * @param ignoreCertificate
	 *            the ignoreCertificate to set
	 */
	public void setIgnoreCertificate(Boolean ignoreCertificate) {
		this.ignoreCertificate = ignoreCertificate;
	}

	/**
	 * @return the connectionPoolSize
	 */
	public Integer getConnectionPoolSize() {
		return connectionPoolSize;
	}

	/**
	 * @param connectionPoolSize
	 *            the connectionPoolSize to set
	 */
	public void setConnectionPoolSize(Integer connectionPoolSize) {
		this.connectionPoolSize = connectionPoolSize;
	}

	/**
	 * @return the connectTimeoutInSec
	 */
	public Integer getConnectTimeoutInSec() {
		return connectTimeoutInSec;
	}

	/**
	 * @param connectTimeoutInSec
	 *            the connectTimeoutInSec to set
	 */
	public void setConnectTimeoutInSec(Integer connectTimeoutInSec) {
		this.connectTimeoutInSec = connectTimeoutInSec;
	}

	public Integer getSocketTimeoutInSec() {
		return socketTimeoutInSec;
	}

	public void setSocketTimeoutInSec(Integer socketTimeoutInSec) {
		this.socketTimeoutInSec = socketTimeoutInSec;
	}

	@Override
	public String toString() {
		return "RestConfigurationInfo [readTimeoutInSec=" + readTimeoutInSec + ", ignoreCertificate="
				+ ignoreCertificate + ", connectionPoolSize=" + connectionPoolSize + ", connectTimeoutInSec="
				+ connectTimeoutInSec + ", socketTimeoutInSec=" + socketTimeoutInSec + "]";
	}

}
