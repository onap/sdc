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

package org.openecomp.sdc.be.externalapi.servlet.representation;

public abstract class AssetMetadata implements IAssetMetadata {
	private String uuid;
	private String invariantUUID;
	private String name;
	private String version;
	private String toscaModelURL;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openecomp.sdc.be.distribution.servlet.representation.IAssetMetadata# getUuid()
	 */
	@Override
	public String getUuid() {
		return uuid;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openecomp.sdc.be.distribution.servlet.representation.IAssetMetadata# setUuid(java.lang.String)
	 */
	@Override
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openecomp.sdc.be.distribution.servlet.representation.IAssetMetadata# getInvariantUUID()
	 */
	@Override
	public String getInvariantUUID() {
		return invariantUUID;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openecomp.sdc.be.distribution.servlet.representation.IAssetMetadata# setInvariantUUID(java.lang.String)
	 */
	@Override
	public void setInvariantUUID(String invariantUUID) {
		this.invariantUUID = invariantUUID;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openecomp.sdc.be.distribution.servlet.representation.IAssetMetadata# getName()
	 */
	@Override
	public String getName() {
		return name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openecomp.sdc.be.distribution.servlet.representation.IAssetMetadata# setName(java.lang.String)
	 */
	@Override
	public void setName(String name) {
		this.name = name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openecomp.sdc.be.distribution.servlet.representation.IAssetMetadata# getVersion()
	 */
	@Override
	public String getVersion() {
		return version;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openecomp.sdc.be.distribution.servlet.representation.IAssetMetadata# setVersion(java.lang.String)
	 */
	@Override
	public void setVersion(String version) {
		this.version = version;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openecomp.sdc.be.distribution.servlet.representation.IAssetMetadata# toscaModelURL()
	 */
	@Override
	public String getToscaModelURL() {
		return toscaModelURL;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openecomp.sdc.be.distribution.servlet.representation.IAssetMetadata# toscaModelURL(java.lang.String)
	 */
	@Override
	public void setToscaModelURL(String toscaModelURL) {
		this.toscaModelURL = toscaModelURL;
	}
}
