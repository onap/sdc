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

package org.openecomp.sdc.be.components.distribution.engine;

import org.openecomp.sdc.common.api.ArtifactTypeEnum;

public interface IArtifactInfo {

	/** Artifact File name */
	String getArtifactName();

	/**
	 * Artifact Type.<br>
	 * Following are valid values : HEAT , DG_XML. <br>
	 * List of values will be extended in post-1510 releases.
	 */
	ArtifactTypeEnum getArtifactType();

	/**
	 * Relative artifact's URL. Should be used in REST GET API to download the artifact's payload.<br>
	 * The full artifact URL will be in the following format :<br>
	 * https://{serverBaseURL}/{resourcePath}<br>
	 * serverBaseURL - Hostname ( ASDC LB FQDN) + optional port <br>
	 * resourcePath - "artifactURL" <br>
	 * Ex : https://asdc.sdc.com/v1/catalog/services/srv1/2.0/resources/aaa/1.0/artifacts/aaa.yml
	 */
	String getArtifactURL();

	/**
	 * Base-64 encoded MD5 checksum of the artifact's payload.<br>
	 * Should be used for data integrity validation when an artifact's payload is downloaded.<br>
	 */
	String getArtifactChecksum();

	/**
	 * Installation timeout. Used by the orchestrator.
	 */
	Integer getArtifactTimeout();

	/**
	 * Artifact description
	 */
	String getArtifactDescription();

}
